package im.swyp.teumteumeat.domains.categoryDocument.persistence.repository;

import im.swyp.teumteumeat.domains.categoryDocument.persistence.entity.CategoryDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CategoryDocumentRepository extends JpaRepository<CategoryDocument, Long> {
    List<CategoryDocument> findAllByGoalId(Long goalId);

    List<CategoryDocument> findAllByCategoryId(Long categoryId);

    boolean existsByGoalIdAndCreatedDateBetween(Long goalId, LocalDateTime start, LocalDateTime end);
}
