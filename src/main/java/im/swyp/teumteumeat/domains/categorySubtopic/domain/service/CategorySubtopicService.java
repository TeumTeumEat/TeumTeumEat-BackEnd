package im.swyp.teumteumeat.domains.categorySubtopic.domain.service;

import im.swyp.teumteumeat.domains.categorySubtopic.persistence.entity.CategorySubtopic;
import im.swyp.teumteumeat.domains.categorySubtopic.persistence.repository.CategorySubtopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategorySubtopicService {

    private final CategorySubtopicRepository subtopicRepository;

    public Optional<CategorySubtopic> findSubtopic(Long categoryId, int durationWeeks, int sequenceIndex) {
        return subtopicRepository.findByCategoryIdAndDurationWeeksAndSequenceIndex(
                categoryId, durationWeeks, sequenceIndex);
    }

    public List<CategorySubtopic> findAllByCategory(Long categoryId, int durationWeeks) {
        return subtopicRepository.findByCategoryIdAndDurationWeeksOrderBySequenceIndex(
                categoryId, durationWeeks);
    }

    public boolean hasSeed(Long categoryId, int durationWeeks) {
        return subtopicRepository.existsByCategoryIdAndDurationWeeks(categoryId, durationWeeks);
    }

    @Transactional
    public void saveAll(List<CategorySubtopic> subtopics) {
        subtopicRepository.saveAll(subtopics);
    }
}
