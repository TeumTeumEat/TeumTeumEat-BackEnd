package im.swyp.teumteumeat.domains.categorySubtopic.persistence.repository;

import im.swyp.teumteumeat.domains.categorySubtopic.persistence.entity.CategorySubtopic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategorySubtopicRepository extends JpaRepository<CategorySubtopic, Long> {

    Optional<CategorySubtopic> findByCategoryIdAndDurationWeeksAndSequenceIndex(
            Long categoryId, int durationWeeks, int sequenceIndex);

    List<CategorySubtopic> findByCategoryIdAndDurationWeeksOrderBySequenceIndex(
            Long categoryId, int durationWeeks);

    boolean existsByCategoryIdAndDurationWeeks(Long categoryId, int durationWeeks);
}
