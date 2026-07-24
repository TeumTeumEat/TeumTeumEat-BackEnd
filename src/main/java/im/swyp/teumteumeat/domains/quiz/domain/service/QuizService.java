package im.swyp.teumteumeat.domains.quiz.domain.service;

import im.swyp.teumteumeat.domains.categoryDocument.persistence.entity.CategoryDocument;
import im.swyp.teumteumeat.domains.document.persistence.entity.Document;
import im.swyp.teumteumeat.domains.document.persistence.entity.DocumentSummary;
import im.swyp.teumteumeat.domains.quiz.domain.constant.QuizResponseCode;
import im.swyp.teumteumeat.domains.quiz.domain.constant.QuizType;
import im.swyp.teumteumeat.domains.quiz.persistence.entity.Quiz;
import im.swyp.teumteumeat.domains.quiz.persistence.repository.QuizRepository;
import im.swyp.teumteumeat.domains.goal.domain.constant.Difficulty;
import im.swyp.teumteumeat.global.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuizService {

        private final QuizRepository quizRepository;

        public List<Quiz> getQuizzesByCategoryDocumentId(Long categoryDocumentId) {
                return quizRepository.findByCategoryDocumentId(categoryDocumentId);
        }

        public List<Quiz> getQuizzesByDocumentId(Long documentId) {
                return quizRepository.findByDocumentId(documentId);
        }

        public List<Quiz> getQuizzesByDocumentSummaryId(Long documentSummaryId) {
                return quizRepository.findByDocumentSummaryId(documentSummaryId);
        }

        public List<Quiz> getUnsolvedCategoryQuizzes(Long categoryDocumentId, Long userId, int limit) {
                return quizRepository.findUnsolvedCategoryQuizzes(categoryDocumentId, userId,
                                org.springframework.data.domain.PageRequest.of(0, limit));
        }

        public List<Quiz> getUnsolvedQuizzesByAttributes(Long categoryDocumentId, Long userId, Difficulty difficulty,
                        String topic, int limit) {
                return quizRepository.findUnsolvedQuizzesByAttributes(categoryDocumentId, userId, difficulty, topic,
                                org.springframework.data.domain.PageRequest.of(0, limit));
        }

        public List<Quiz> getUnsolvedDocumentQuizzes(Long documentSummaryId, Long userId, int limit) {
                return quizRepository.findUnsolvedByDocumentSummaryId(documentSummaryId, userId,
                                org.springframework.data.domain.PageRequest.of(0, limit));
        }

        public Quiz getQuizById(Long quizId) {
                return quizRepository.findById(quizId)
                                .orElseThrow(() -> new BaseException(QuizResponseCode.NOT_FOUND_QUIZ));
        }

        @Transactional
        public void deleteQuiz(Long quizId) {
                quizRepository.deleteById(quizId);
        }

        @Transactional(propagation = Propagation.NOT_SUPPORTED)
        public Quiz buildQuizFromCategoryDocument(
                        CategoryDocument document,
                        String question, String options, String answer, QuizType type, String explanation,
                        String topic, Difficulty difficulty) {
                return buildQuiz(document, null, null, question, options, answer, type, explanation, topic, difficulty);
        }

        @Transactional(propagation = Propagation.NOT_SUPPORTED)
        public Quiz buildQuizFromPdfDocument(
                        Document document,
                        DocumentSummary documentSummary,
                        String question, String options, String answer, QuizType type, String explanation,
                        String topic, Difficulty difficulty) {
                return buildQuiz(null, document, documentSummary, question, options, answer, type, explanation, topic,
                                difficulty);
        }

        private Quiz buildQuiz(CategoryDocument categoryDocument, Document document, DocumentSummary documentSummary,
                        String question, String options, String answer, QuizType type, String explanation,
                        String topic, Difficulty difficulty) {
                return Quiz.builder()
                                .categoryDocument(categoryDocument)
                                .document(document)
                                .documentSummary(documentSummary)
                                .content(question)
                                .options(options)
                                .answer(answer)
                                .description(explanation)
                                .quizType(type)
                                .topic(topic)
                                .difficulty(difficulty)
                                .build();
        }

        @Transactional
        public void saveQuizzes(List<Quiz> quizzes) {
                quizRepository.saveAll(quizzes);
        }

}
