package im.swyp.teumteumeat.domains.quiz.domain.service;

import im.swyp.teumteumeat.domains.categoryDocument.persistence.entity.CategoryDocument;
import im.swyp.teumteumeat.domains.quiz.application.dto.request.QuizSubmissionRequest;
import im.swyp.teumteumeat.domains.quiz.application.dto.response.QuizSubmissionResponse;
import im.swyp.teumteumeat.domains.quiz.domain.constant.QuizType;
import im.swyp.teumteumeat.domains.quiz.persistence.entity.Quiz;
import im.swyp.teumteumeat.domains.quiz.persistence.entity.UserQuizHistory;
import im.swyp.teumteumeat.domains.quiz.persistence.repository.QuizRepository;
import im.swyp.teumteumeat.domains.quiz.persistence.repository.UserQuizHistoryRepository;
import im.swyp.teumteumeat.domains.user.domain.service.UserService;
import im.swyp.teumteumeat.domains.user.persistence.entity.UserEntity;
import im.swyp.teumteumeat.global.common.CommonResponseCode;
import im.swyp.teumteumeat.global.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuizService {

        private final QuizRepository quizRepository;
        private final UserQuizHistoryRepository userQuizHistoryRepository;
        private final UserService userService;

        public List<Quiz> getQuizzesByDocumentId(Long documentId) {
                return quizRepository.findByCategoryDocumentId(documentId);
        }

        public Quiz getQuizById(Long quizId) {
                return quizRepository.findById(quizId)
                                .orElseThrow(() -> new BaseException(CommonResponseCode.NOT_FOUND));
        }

        @Transactional
        public void deleteQuiz(Long quizId) {
                quizRepository.deleteById(quizId);
        }

        @Transactional
        public void createQuiz(
                        CategoryDocument document,
                        String question, String options, String answer, String type, String explanation) {
                // QuizType 매핑 로직 필요 (String -> Enum)
                QuizType quizType = "OX".equalsIgnoreCase(type)
                                ? QuizType.OX
                                : QuizType.MCQ;

                Quiz quiz = Quiz.builder()
                                .categoryDocument(document)
                                .content(question)
                                .options(options)
                                .answer(answer)
                                .description(explanation)
                                .quizType(quizType)
                                .build();

                quizRepository.save(quiz);
        }

        @Transactional
        public QuizSubmissionResponse submitQuiz(Long userId, QuizSubmissionRequest request) {
                // 1. 사용자 및 퀴즈 조회
                UserEntity user = userService.getUserById(userId);
                Quiz quiz = quizRepository.findById(request.quizId())
                                .orElseThrow(() -> new BaseException(CommonResponseCode.NOT_FOUND));

                // 2. 정답 채점 (단순 문자열 비교, 필요시 로직 고도화)
                boolean isCorrect = quiz.getAnswer().trim().equalsIgnoreCase(request.userAnswer().trim());

                // 3. 기록 저장 (이미 푼 기록이 있어도 중복 저장 허용 -> 복습 개념)
                UserQuizHistory history = UserQuizHistory.builder()
                                .user(user)
                                .quiz(quiz)
                                .isCorrect(isCorrect)
                                .build();

                userQuizHistoryRepository.save(history);

                // 4. 결과 반환
                return QuizSubmissionResponse.builder()
                                .isCorrect(isCorrect)
                                .correctAnswer(quiz.getAnswer())
                                .explanation(quiz.getDescription())
                                .build();
        }
}
