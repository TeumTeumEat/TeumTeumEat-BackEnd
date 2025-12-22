package im.swyp.teumteumeat.domains.goal.persistence.repository;

import im.swyp.teumteumeat.domains.goal.persistence.entity.Goal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GoalRepository extends JpaRepository<Goal, Long> {
    List<Goal> findAllByUserId(Long userId);

    Optional<Goal> findTopByUserIdAndCategoryIdOrderByCreatedDateDesc(Long userId, Long categoryId);
}
