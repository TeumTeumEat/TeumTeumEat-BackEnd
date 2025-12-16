package im.swyp.teumteumeat.domains.quiz.persistence.repository;

import im.swyp.teumteumeat.domains.quiz.persistence.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findByCategoryDocumentId(Long categoryDocumentId);
}
