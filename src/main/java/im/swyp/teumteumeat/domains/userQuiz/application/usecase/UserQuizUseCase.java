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
import im.swyp.teumteumeat.global.annotation.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQuizUseCase {

    private final QuizService quizService;
    private final UserQuizService userQuizService;
    private final UserService userService;
    private final QuizMapper quizMapper;

    private final QuizUseCase quizUseCase;

    @Transactional
    public QuizSubmissionResponse submitQuiz(Long userId, QuizSubmissionRequest request) {
        UserEntity user = userService.getUserById(userId);
        Quiz quiz = quizService.getQuizById(request.quizId());

        boolean isCorrect = quiz.getAnswer().trim().equalsIgnoreCase(request.userAnswer().trim());

        UserQuiz userQuiz = UserQuiz.builder()
                .user(user)
                .quiz(quiz)
                .isCorrect(isCorrect)
                .build();

        userQuizService.saveUserQuiz(userQuiz);

        return QuizSubmissionResponse.builder()
                .isCorrect(isCorrect)
                .correctAnswer(quiz.getAnswer())
                .explanation(quiz.getDescription())
                .build();
    }

    @Transactional
    public java.util.List<QuizSetResponse> getQuizzesForSolving(
            Long documentId, Long userId) {
        // 사용자가 푼 적 없는 퀴즈만 제공
        java.util.List<Quiz> quizzesUnsolved = quizService.getUnsolvedQuizzes(documentId, userId, 10);

        // 해당 카테고리 자료 퀴즈를 사용자가 다 풀었을 시 퀴즈 추가 생성
        if (quizzesUnsolved.isEmpty()) {
            quizUseCase.createQuizzesForDocument(documentId);
            quizzesUnsolved = quizService.getUnsolvedQuizzes(documentId, userId, 10);
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
