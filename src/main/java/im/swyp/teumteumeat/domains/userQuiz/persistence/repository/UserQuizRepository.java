package im.swyp.teumteumeat.domains.userQuiz.persistence.repository;

import im.swyp.teumteumeat.domains.userQuiz.persistence.entity.UserQuiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserQuizRepository extends JpaRepository<UserQuiz, Long> {

    @Query("SELECT h.quiz.categoryDocument.id FROM UserQuiz h WHERE h.user.id = :userId")
    List<Long> findConsumedDocumentIdsByUserId(@Param("userId") Long userId);
}
