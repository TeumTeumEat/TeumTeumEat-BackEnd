package im.swyp.teumteumeat.domains.categoryDocument.persistence.repository;

import im.swyp.teumteumeat.domains.categoryDocument.persistence.entity.CategoryDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryDocumentRepository extends JpaRepository<CategoryDocument, Long> {
    List<CategoryDocument> findAllByGoalId(Long goalId);

    List<CategoryDocument> findAllByCategoryId(Long categoryId);
}
