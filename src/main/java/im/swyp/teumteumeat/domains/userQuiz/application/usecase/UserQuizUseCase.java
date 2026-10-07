package im.swyp.teumteumeat.domains.userQuiz.application.usecase;

import im.swyp.teumteumeat.domains.categoryDocument.domain.service.CategoryDocumentService;
import im.swyp.teumteumeat.domains.document.domain.service.DocumentSummaryService;
import im.swyp.teumteumeat.domains.document.persistence.entity.DocumentSummary;
import im.swyp.teumteumeat.domains.league.domain.event.SnackEarnedEvent;
import im.swyp.teumteumeat.domains.league.domain.service.SnackHistoryService;
import im.swyp.teumteumeat.domains.league.persistence.entity.SnackHistory;
import im.swyp.teumteumeat.domains.quiz.application.mapper.QuizMapper;
import im.swyp.teumteumeat.domains.quiz.application.usecase.QuizUseCase;
import im.swyp.teumteumeat.domains.quiz.domain.constant.QuizResponseCode;
import im.swyp.teumteumeat.domains.quiz.domain.service.QuizService;
import im.swyp.teumteumeat.domains.quiz.persistence.entity.Quiz;
import im.swyp.teumteumeat.domains.user.domain.service.UserService;
import im.swyp.teumteumeat.domains.user.persistence.entity.UserEntity;
import im.swyp.teumteumeat.domains.userQuiz.application.dto.request.QuizSubmissionRequest;
import im.swyp.teumteumeat.domains.userQuiz.application.dto.response.QuizSetResponse;
import im.swyp.teumteumeat.domains.userQuiz.application.dto.response.QuizSubmissionResponse;
import im.swyp.teumteumeat.domains.userQuiz.application.dto.response.UserQuizStatusResponse;
import im.swyp.teumteumeat.domains.userQuiz.application.mapper.UserQuizMapper;
import im.swyp.teumteumeat.domains.userQuiz.domain.service.UserQuizService;
import im.swyp.teumteumeat.domains.userQuiz.persistence.entity.UserQuiz;
import im.swyp.teumteumeat.domains.goal.domain.constant.GoalType;
import im.swyp.teumteumeat.domains.goal.domain.service.GoalService;
import im.swyp.teumteumeat.domains.goal.persistence.entity.Goal;
import im.swyp.teumteumeat.domains.categoryDocument.persistence.entity.CategoryDocument;
import im.swyp.teumteumeat.global.common.CommonResponseCode;

import im.swyp.teumteumeat.global.annotation.UseCase;
import im.swyp.teumteumeat.global.exception.BaseException;
import lombok.RequiredArgsConstructor;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@UseCase
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQuizUseCase {

    private final UserQuizService userQuizService;
    private final QuizService quizService;
    private final QuizUseCase quizUseCase;
    private final UserService userService;
    private final QuizMapper quizMapper;

    private final GoalService goalService;
    private final CategoryDocumentService categoryDocumentService;
    private final DocumentSummaryService documentSummaryService;
    private final UserQuizMapper userQuizMapper;
    private final SnackHistoryService snackHistoryService;
    private final ApplicationEventPublisher eventPublisher;

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
                                    .goal(user.getCurrentGoal())
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

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public List<QuizSetResponse> getQuizzesForSolving(
            Long documentId, Long userId, GoalType documentType) {
        // 이동시간 기반 문제 수 계산
        int quizCount = quizUseCase.calculateQuestionCount(userId);

        // 사용자가 푼 적 없는 퀴즈만 제공
        List<Quiz> quizzesUnsolved;
        if (GoalType.DOCUMENT == documentType) {
            DocumentSummary latestSummary = documentSummaryService.getLatestSummaryByDocumentId(documentId)
                    .orElse(null);
            if (latestSummary == null) {
                throw new BaseException(QuizResponseCode.QUIZ_GENERATING);
            }
            quizzesUnsolved = quizService.getUnsolvedDocumentQuizzes(latestSummary.getId(), userId, quizCount);
            
            if (quizzesUnsolved.isEmpty()) {
                List<Quiz> allQuizzes = quizService.getQuizzesByDocumentSummaryId(latestSummary.getId());
                if (allQuizzes.isEmpty()) {
                    throw new BaseException(QuizResponseCode.QUIZ_GENERATING);
                }
            }
        } else {
            quizzesUnsolved = getPrioritizedQuizzes(documentId, userId, quizCount);
            // getPrioritizedQuizzes에서 빈 리스트가 돌아왔을 경우
            if (quizzesUnsolved.isEmpty()) {
                return Collections.emptyList();
            }
        }

        // 퀴즈 수가 여전히 부족하면(아예 없거나) -> 생성 로직
        if (quizzesUnsolved.isEmpty()) {
            if (GoalType.DOCUMENT == documentType) {
                // PDF 문서는 자동 생성은 보류 (개인만 접근 가능)
            } else {
                // 프롬프트가 있는 경우에만 퀴즈 생성
                // 프롬프트가 없는(Default) 경우에는 기존 퀴즈만 제공

                CategoryDocument document = categoryDocumentService.getDocumentWithCategoryById(documentId);
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

    // 부족분 생성(락 포함) 로직은 QuizUseCase.ensureQuizzesAvailable로 옮겨서
    // 요약글 생성 직후 프리페치와 공유한다.
    private List<Quiz> getPrioritizedQuizzes(Long documentId, Long userId, int quizCount) {
        return quizUseCase.ensureQuizzesAvailable(documentId, userId, quizCount);
    }

    public QuizSetResponse getQuizForSolving(
            Long quizId) {
        Quiz quiz = quizService.getQuizById(quizId);
        return quizMapper.toQuestionResponse(quiz);
    }

    public UserQuizStatusResponse getUserQuizStatus(Long userId) {
        boolean hasSolvedToday = hasSolvedAnyQuizToday(userId);
        boolean hasSolvedEver = hasSolvedAnyQuizEver(userId);
        boolean hasGeneratedContent = hasCreatedDocumentToday(userId);
        boolean isQuizGuideSeen = isQuizGuideSeen(userId);

        UserEntity userEntity = userService.getUserById(userId);

        return userQuizMapper.toStatusResponse(
                userEntity,
                hasSolvedToday,
                hasSolvedEver,
                hasGeneratedContent,
                isQuizGuideSeen);
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

    @Transactional
    public void completeQuizSet(Long userId) {
        UserEntity user = userService.getUserById(userId);

        if (!user.canSolveDailyQuiz()) {
            throw new BaseException(
                    QuizResponseCode.TODAY_QUOTA_EXCEEDED);
        }

        user.consumeQuizCount();

        // 리그 스낵 적립 (퀴즈 세트 1회 완료 = 1 스낵), 커밋 후 리그 랭킹 캐시 삭제
        SnackHistory snack = snackHistoryService.earnSnack(user);
        eventPublisher.publishEvent(new SnackEarnedEvent(snack.getCreatedDate()));

        Goal currentGoal = user.getCurrentGoal();
        if (currentGoal != null && !currentGoal.isCompleted()) {
            currentGoal.incrementCompletedQuizSetCount();
        }
    }

    public boolean isQuizGuideSeen(Long userId) {
        UserEntity user = userService.getUserById(userId);
        return user.isQuizGuideSeen();
    }

    @Transactional
    public void claimAdReward(Long userId) {
        UserEntity user = userService.getUserById(userId);
        user.claimAdReward();
    }

    @Transactional
    public void testResetAdReward(Long userId) {
        UserEntity user = userService.getUserById(userId);
        
        user.testResetAdReward();
    }

    @Transactional
    public void testAddQuizCount(Long userId, int count) {
        UserEntity user = userService.getUserById(userId);
        
        user.addAvailableQuizCount(count);
    }

    @Transactional
    public void testResetGoalStatus(Long userId, Long goalId) {
        UserEntity user = userService.getUserById(userId);
        
        Goal targetGoal;
        if (goalId != null) {
            targetGoal = user.getGoals().stream()
                    .filter(g -> g.getId().equals(goalId))
                    .findFirst()
                    .orElseThrow(() -> new BaseException(CommonResponseCode.NOT_FOUND));
        } else {
            targetGoal = user.getCurrentGoal();
        }
        
        if (targetGoal != null) {
            targetGoal.testResetGoalStatus();
        }
    }
}
