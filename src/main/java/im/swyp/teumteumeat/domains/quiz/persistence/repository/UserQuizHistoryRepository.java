package im.swyp.teumteumeat.domains.quiz.persistence.repository;

import im.swyp.teumteumeat.domains.quiz.persistence.entity.UserQuizHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserQuizHistoryRepository extends JpaRepository<UserQuizHistory, Long> {

    @Query("SELECT h.quiz.categoryDocument.id FROM UserQuizHistory h WHERE h.user.id = :userId")
    List<Long> findConsumedDocumentIdsByUserId(@Param("userId") Long userId);
}
