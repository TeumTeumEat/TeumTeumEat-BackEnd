package im.swyp.teumteumeat.domains.userQuiz.persistence.repository;

import im.swyp.teumteumeat.domains.notification.application.mapper.UserStudyDateMapping;
import im.swyp.teumteumeat.domains.userQuiz.persistence.entity.UserQuiz;
import im.swyp.teumteumeat.domains.user.persistence.entity.UserEntity;
import im.swyp.teumteumeat.domains.quiz.persistence.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserQuizRepository extends JpaRepository<UserQuiz, Long> {

        @Query("SELECT h.quiz.categoryDocument.id FROM UserQuiz h WHERE h.user.id = :userId")
        List<Long> findConsumedDocumentIdsByUserId(@Param("userId") Long userId);

        @Query("SELECT h.quiz.document.id FROM UserQuiz h WHERE h.user.id = :userId")
        List<Long> findConsumedPdfDocumentIdsByUserId(@Param("userId") Long userId);

        List<UserQuiz> findAllByUserIdAndCreatedDateBetween(Long userId, LocalDateTime start,
                        LocalDateTime end);

        List<UserQuiz> findAllByUserIdOrderByCreatedDateDesc(Long userId);

        Optional<UserQuiz> findByUserAndQuizAndCreatedDateBetween(
                        UserEntity user,
                        Quiz quiz,
                        LocalDateTime start,
                        LocalDateTime end);

        @Query(value = "SELECT COUNT(DISTINCT CAST(created_date AS date)) FROM user_quiz WHERE user_id = :userId", nativeQuery = true)
        int countDistinctDaysByUserId(@Param("userId") Long userId);

        boolean existsByUserIdAndQuiz_CategoryDocument_Category_IdAndCreatedDateBetween(Long userId, Long categoryId,
                        LocalDateTime start, LocalDateTime end);

        boolean existsByUserIdAndQuiz_Document_Goal_IdAndCreatedDateBetween(Long userId, Long goalId,
                        LocalDateTime start, LocalDateTime end);

        boolean existsByUserIdAndQuiz_DocumentSummary_Id(Long userId, Long documentSummaryId);

        void deleteAllByUserId(Long userId);

        @Query("SELECT DISTINCT uq.user FROM UserQuiz uq WHERE uq.createdDate BETWEEN :start AND :end")
        List<UserEntity> findDistinctUsersByQuizDate(LocalDateTime start, LocalDateTime end);

        @Query("SELECT u.user.id AS userId, CAST(u.createdDate AS date) AS studyDate " +
                        "FROM UserQuiz u " +
                        "WHERE u.user.id IN :userIds " +
                        "GROUP BY u.user.id, CAST(u.createdDate AS date) " +
                        "ORDER BY u.user.id, CAST(u.createdDate AS date) DESC")
        List<UserStudyDateMapping> findUserStudyDates(List<Long> userIds);
}
