package im.swyp.teumteumeat.domains.userQuiz.persistence.repository;

import im.swyp.teumteumeat.domains.userQuiz.persistence.entity.UserQuiz;
import im.swyp.teumteumeat.domains.user.persistence.entity.UserEntity;
import im.swyp.teumteumeat.domains.quiz.persistence.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserQuizRepository extends JpaRepository<UserQuiz, Long> {

        @Query("SELECT h.quiz.categoryDocument.id FROM UserQuiz h WHERE h.user.id = :userId")
        List<Long> findConsumedDocumentIdsByUserId(@Param("userId") Long userId);

        List<UserQuiz> findAllByUserIdAndCreatedDateBetween(Long userId, java.time.LocalDateTime start,
                        java.time.LocalDateTime end);

        Optional<UserQuiz> findByUserAndQuizAndCreatedDateBetween(
                        UserEntity user,
                        Quiz quiz,
                        java.time.LocalDateTime start,
                        java.time.LocalDateTime end);

        @Query("SELECT DISTINCT CAST(u.createdDate AS date) FROM UserQuiz u WHERE u.user.id = :userId ORDER BY CAST(u.createdDate AS date) DESC")
        List<java.sql.Date> findDistinctDaysByUserId(@Param("userId") Long userId);
}
