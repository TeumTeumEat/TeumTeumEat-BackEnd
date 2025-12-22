package im.swyp.teumteumeat.domains.userQuiz.application.usecase;

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
import im.swyp.teumteumeat.domains.userQuiz.persistence.repository.UserQuizRepository;
import im.swyp.teumteumeat.domains.goal.domain.constant.GoalType;
import im.swyp.teumteumeat.global.annotation.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@UseCase
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQuizUseCase {

    private final QuizService quizService;
    private final UserQuizService userQuizService;
    private final UserQuizRepository userQuizRepository;
    private final UserService userService;
    private final QuizMapper quizMapper;

    private final QuizUseCase quizUseCase;

    @Transactional
    public QuizSubmissionResponse submitQuiz(Long userId, QuizSubmissionRequest request) {
        UserEntity user = userService.getUserById(userId);
        Quiz quiz = quizService.getQuizById(request.quizId());

        boolean isCorrect = quiz.getAnswer().trim().equalsIgnoreCase(request.userAnswer().trim());

        // 오늘 날짜 범위 계산
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDateTime startOfDay = today.atStartOfDay();
        java.time.LocalDateTime endOfDay = today.atTime(java.time.LocalTime.MAX);

        // 오늘 이미 푼 기록이 있는지 확인 (Date-based Upsert)
        userQuizRepository.findByUserAndQuizAndCreatedDateBetween(user, quiz, startOfDay, endOfDay)
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
        // 사용자가 푼 적 없는 퀴즈만 제공
        List<Quiz> quizzesUnsolved;
        if (GoalType.DOCUMENT == documentType) {
            quizzesUnsolved = quizService.getUnsolvedDocumentQuizzes(documentId, userId, 10);
        } else {
            quizzesUnsolved = quizService.getUnsolvedCategoryQuizzes(documentId, userId, 10);
        }

        // 해당 카테고리 자료 퀴즈(모든 유저가 접근 가능)를 사용자가 다 풀었을 시 퀴즈 추가 생성
        if (quizzesUnsolved.isEmpty()) {
            if (GoalType.DOCUMENT == documentType) {
                // PDF 문서는 자동 생성은 보류 (개인만 접근 가능)
            } else {
                quizUseCase.createQuizzesForDocument(documentId);
                quizzesUnsolved = quizService.getUnsolvedCategoryQuizzes(documentId, userId, 10);
            }
        }

        return quizzesUnsolved.stream()
                .map(quizMapper::toQuestionResponse)
                .toList();
    }

    public QuizSetResponse getQuizForSolving(
            Long quizId) {
        Quiz quiz = quizService.getQuizById(quizId);
        return quizMapper.toQuestionResponse(quiz);
    }
}
