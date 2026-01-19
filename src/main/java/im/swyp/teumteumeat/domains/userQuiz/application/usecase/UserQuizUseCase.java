package im.swyp.teumteumeat.domains.userQuiz.application.usecase;

import im.swyp.teumteumeat.domains.categoryDocument.domain.service.CategoryDocumentService;
import im.swyp.teumteumeat.domains.document.domain.service.DocumentSummaryService;
import im.swyp.teumteumeat.domains.quiz.application.mapper.QuizMapper;
import im.swyp.teumteumeat.domains.quiz.application.usecase.QuizUseCase;
import im.swyp.teumteumeat.domains.quiz.domain.service.QuizService;
import im.swyp.teumteumeat.domains.quiz.persistence.entity.Quiz;
import im.swyp.teumteumeat.domains.user.domain.service.UserService;
import im.swyp.teumteumeat.domains.user.persistence.entity.UserEntity;
import im.swyp.teumteumeat.domains.userQuiz.application.dto.request.QuizSubmissionRequest;
import im.swyp.teumteumeat.domains.userQuiz.application.dto.response.QuizSetResponse;
import im.swyp.teumteumeat.domains.userQuiz.application.dto.response.QuizSubmissionResponse;
import im.swyp.teumteumeat.domains.userQuiz.domain.service.UserQuizService;
import im.swyp.teumteumeat.domains.userQuiz.persistence.entity.UserQuiz;
import im.swyp.teumteumeat.domains.goal.domain.constant.GoalType;
import im.swyp.teumteumeat.domains.goal.domain.constant.Difficulty;
import im.swyp.teumteumeat.domains.goal.domain.service.GoalService;
import im.swyp.teumteumeat.domains.goal.persistence.entity.Goal;
import im.swyp.teumteumeat.domains.categoryDocument.persistence.entity.CategoryDocument;

import im.swyp.teumteumeat.global.exception.BaseException;
import im.swyp.teumteumeat.global.annotation.UseCase;
import im.swyp.teumteumeat.global.component.DistributedLockFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@UseCase
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQuizUseCase {

    private final QuizService quizService;
    private final UserQuizService userQuizService;
    private final UserService userService;
    private final QuizMapper quizMapper;
    private final DistributedLockFacade distributedLockFacade;

    private final GoalService goalService;
    private final CategoryDocumentService categoryDocumentService;
    private final DocumentSummaryService documentSummaryService;
    private final QuizUseCase quizUseCase;

    @Transactional
    public QuizSubmissionResponse submitQuiz(Long userId, QuizSubmissionRequest request) {
        UserEntity user = userService.getUserById(userId);
        Quiz quiz = quizService.getQuizById(request.quizId());

        boolean isCorrect = quiz.getAnswer().trim().equalsIgnoreCase(request.userAnswer().trim());

        // 오늘 날짜 범위 계산
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        // 오늘 이미 푼 기록이 있는지 확인 (Date-based Upsert)
        userQuizService.getQuizByDate(user, quiz, startOfDay, endOfDay)
                .ifPresentOrElse(
                        existingUserQuiz -> existingUserQuiz.updateResult(isCorrect), // 있으면 업데이트
                        () -> {
                            // 없으면 새로 생성
                            UserQuiz newUserQuiz = UserQuiz.builder()
                                    .user(user)
                                    .quiz(quiz)
                                    .isCorrect(isCorrect)
                                    .build();
                            userQuizService.saveUserQuiz(newUserQuiz);
                        });

        return QuizSubmissionResponse.builder()
                .isCorrect(isCorrect)
                .correctAnswer(quiz.getAnswer())
                .explanation(quiz.getDescription())
                .build();
    }

    @Transactional
    public List<QuizSetResponse> getQuizzesForSolving(
            Long documentId, Long userId, GoalType documentType) {
        // 이동시간 기반 문제 수 계산
        int quizCount = quizUseCase.calculateQuestionCount(userId);

        // 사용자가 푼 적 없는 퀴즈만 제공
        List<Quiz> quizzesUnsolved;
        if (GoalType.DOCUMENT == documentType) {
            quizzesUnsolved = quizService.getUnsolvedDocumentQuizzes(documentId, userId, quizCount);
        } else {
            quizzesUnsolved = getPrioritizedQuizzes(documentId, userId, quizCount);
        }

        // 퀴즈 수가 여전히 부족하면(아예 없거나) -> 생성 로직
        if (quizzesUnsolved.isEmpty()) {
            if (GoalType.DOCUMENT == documentType) {
                // PDF 문서는 자동 생성은 보류 (개인만 접근 가능)
            } else {
                // 프롬프트가 있는 경우에만 퀴즈 생성
                // 프롬프트가 없는(Default) 경우에는 기존 퀴즈만 제공

                CategoryDocument document = categoryDocumentService.getDocumentById(documentId);
                Goal goal = goalService.findLatestGoal(userId, document.getCategory().getId());
                boolean hasCustomPrompt = goal.getPrompt() != null && !goal.getPrompt().isBlank();

                if (hasCustomPrompt) {
                    quizUseCase.createQuizzesForDocument(documentId, userId, quizCount);
                    quizzesUnsolved = getPrioritizedQuizzes(documentId, userId, quizCount);
                }
            }
        }

        return quizzesUnsolved.stream()
                .map(quizMapper::toQuestionResponse)
                .toList();
    }

    private List<Quiz> getPrioritizedQuizzes(Long documentId, Long userId, int quizCount) {
        // 우선 유저의 Goal (Difficulty, Prompt)과 일치하는 퀴즈 조회
        CategoryDocument document = categoryDocumentService.getDocumentById(documentId);
        Goal goal = goalService.findLatestGoal(userId, document.getCategory().getId());

        Difficulty targetDifficulty = goal.getDifficulty();
        String rawTopic = truncateTopic(goal.getPrompt());
        boolean isDefaultPrompt = rawTopic == null || rawTopic.isBlank();
        String targetTopic = isDefaultPrompt ? "전반적인 내용" : rawTopic;

        // 1단계: 조건에 맞는 퀴즈 조회
        // (프롬프트가 없는 경우: 기존에 생성된 "전반적인 내용" 퀴즈들을 최대한 활용)
        List<Quiz> priorityQuizzes = quizService.getUnsolvedQuizzesByAttributes(documentId, userId,
                targetDifficulty, targetTopic, quizCount);

        // 2-1. 부족한 경우 -> 부족한 만큼 추가 생성 시도 (다른 난이도/토픽 섞지 않음)
        // 2-1. 부족한 경우 -> 부족한 만큼 추가 생성 시도 (다른 난이도/토픽 섞지 않음)
        if (priorityQuizzes.size() < quizCount) {
            String lockKey = "lock:quiz:generation:" + documentId + ":" + userId;

            try {
                priorityQuizzes = distributedLockFacade.executeWithLock(lockKey, 3, 60, TimeUnit.SECONDS, () -> {
                    // Double-Check: 락 획득 후 다시 한 번 개수 확인
                    List<Quiz> currentQuizzes = quizService.getUnsolvedQuizzesByAttributes(documentId, userId,
                            targetDifficulty, targetTopic, quizCount);

                    if (currentQuizzes.size() < quizCount) {
                        int remainingCount = quizCount - currentQuizzes.size();
                        quizUseCase.createQuizzesForDocument(documentId, userId, remainingCount);

                        // 재생성 후 최종 조회
                        return quizService.getUnsolvedQuizzesByAttributes(documentId, userId,
                                targetDifficulty, targetTopic, quizCount);
                    }
                    return currentQuizzes;
                });
            } catch (BaseException e) {
                // 락 획득 실패 시 (Timeout) -> 기존 조회된 것만 반환 (예외 무시)
            }
        }

        // 2-2. 프롬프트가 '있는' 경우이고, 여전히 부족
        // -> 위 getQuizzesForSolving에서 createQuizzesForDocument()를 호출하여 추가 생성
        return priorityQuizzes;
    }

    private String truncateTopic(String topic) {
        if (topic != null && topic.length() > 30) {
            return topic.substring(0, 30);
        }
        return topic;
    }

    public QuizSetResponse getQuizForSolving(
            Long quizId) {
        Quiz quiz = quizService.getQuizById(quizId);
        return quizMapper.toQuestionResponse(quiz);
    }

    public boolean hasSolvedAnyQuizToday(Long userId) {
        return userQuizService.hasSolvedAnyQuizToday(userId);
    }

    public boolean hasSolvedAnyQuizEver(Long userId) {
        return userQuizService.hasSolvedAnyQuizEver(userId);
    }

    public boolean hasCreatedDocumentToday(Long userId) {
        return categoryDocumentService.hasDocumentCreatedToday(userId) ||
                documentSummaryService.hasSummaryCreatedToday(userId);
    }

    @Transactional
    public void completeQuizGuide(Long userId) {
        UserEntity user = userService.getUserById(userId);
        user.completeQuizGuide();
    }

    public boolean isQuizGuideSeen(Long userId) {
        UserEntity user = userService.getUserById(userId);
        return user.isQuizGuideSeen();
    }
}
