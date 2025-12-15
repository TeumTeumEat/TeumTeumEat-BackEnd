package im.swyp.teumteumeat.domains.category.persistence.repository;

import im.swyp.teumteumeat.domains.category.persistence.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
