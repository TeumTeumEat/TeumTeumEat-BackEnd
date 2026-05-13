package im.swyp.teumteumeat.domains.categoryDocument.persistence.repository;

import im.swyp.teumteumeat.domains.categoryDocument.persistence.entity.CategoryDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryDocumentRepository extends JpaRepository<CategoryDocument, Long> {
    List<CategoryDocument> findAllByGoalId(Long goalId);

    boolean existsByGoalIdAndCreatedDateBetween(Long goalId, LocalDateTime start, LocalDateTime end);

    boolean existsByGoal_User_IdAndCreatedDateBetween(Long userId, LocalDateTime start, LocalDateTime end);

    CategoryDocument findTopByCategoryIdOrderByIdDesc(Long categoryId);

    List<CategoryDocument> findAllByCategoryIdAndGoalIsNull(Long categoryId);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = { "category", "goal" })
    Optional<CategoryDocument> findWithCategoryAndGoalById(Long id);

    Optional<CategoryDocument> findTopByGoal_IdOrderByCreatedDateDesc(Long goalId);
}
