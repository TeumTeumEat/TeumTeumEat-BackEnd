package im.swyp.teumteumeat.domains.quiz.domain.service;

import im.swyp.teumteumeat.domains.categoryDocument.persistence.entity.CategoryDocument;
import im.swyp.teumteumeat.domains.quiz.domain.constant.QuizType;
import im.swyp.teumteumeat.domains.quiz.persistence.entity.Quiz;
import im.swyp.teumteumeat.domains.quiz.persistence.repository.QuizRepository;
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

        public List<Quiz> getQuizzesByDocumentId(Long documentId) {
                return quizRepository.findByCategoryDocumentId(documentId);
        }

        public List<Quiz> getUnsolvedQuizzes(Long documentId, Long userId, int limit) {
                return quizRepository.findUnsolvedQuizzes(documentId, userId,
                                org.springframework.data.domain.PageRequest.of(0, limit));
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

}
