package im.swyp.teumteumeat.domains.quiz.persistence.repository;

import im.swyp.teumteumeat.domains.quiz.persistence.entity.Quiz;
import im.swyp.teumteumeat.domains.goal.domain.constant.Difficulty;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
        List<Quiz> findByCategoryDocumentId(Long categoryDocumentId);

        List<Quiz> findByDocumentId(Long documentId);

        List<Quiz> findByDocumentSummaryId(Long documentSummaryId);

        @Query("SELECT q FROM Quiz q WHERE q.categoryDocument.id = :documentId " +
                        "AND q.difficulty = :difficulty " +
                        "AND q.topic = :topic " +
                        "AND q.id NOT IN (SELECT uq.quiz.id FROM UserQuiz uq WHERE uq.user.id = :userId) " +
                        "ORDER BY function('RAND')")
        List<Quiz> findUnsolvedQuizzesByAttributes(@Param("documentId") Long documentId,
                        @Param("userId") Long userId,
                        @Param("difficulty") Difficulty difficulty,
                        @Param("topic") String topic,
                        Pageable pageable);

        @Query("SELECT q FROM Quiz q WHERE q.categoryDocument.id = :documentId " +
                        "AND q.id NOT IN (SELECT uq.quiz.id FROM UserQuiz uq WHERE uq.user.id = :userId) " +
                        "ORDER BY function('RAND')")
        List<Quiz> findUnsolvedCategoryQuizzes(@Param("documentId") Long documentId,
                        @Param("userId") Long userId, Pageable pageable);

        @Query("SELECT q FROM Quiz q WHERE q.documentSummary.id = :documentSummaryId " +
                        "AND q.id NOT IN (SELECT uq.quiz.id FROM UserQuiz uq WHERE uq.user.id = :userId) " +
                        "ORDER BY function('RAND')")
        List<Quiz> findUnsolvedByDocumentSummaryId(@Param("documentSummaryId") Long documentSummaryId,
                        @Param("userId") Long userId, Pageable pageable);
}
