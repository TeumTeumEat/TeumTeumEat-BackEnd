package im.swyp.teumteumeat.domains.categoryDocument.domain.service;

import im.swyp.teumteumeat.domains.categoryDocument.domain.constant.CategoryDocumentResponseCode;
import im.swyp.teumteumeat.domains.categoryDocument.persistence.entity.CategoryDocument;
import im.swyp.teumteumeat.domains.goal.persistence.entity.Goal;
import im.swyp.teumteumeat.domains.categoryDocument.persistence.repository.CategoryDocumentRepository;
import im.swyp.teumteumeat.global.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryDocumentService {

    private final CategoryDocumentRepository categoryDocumentRepository;

    public List<CategoryDocument> getDocumentsByGoalId(Long goalId) {
        return categoryDocumentRepository.findAllByGoalId(goalId);
    }

    public List<CategoryDocument> getCommonDocuments(Long categoryId) {
        return categoryDocumentRepository.findAllByCategoryIdAndGoalIsNull(categoryId);
    }

    public boolean existsByGoalIdAndDate(Long goalId, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);
        return categoryDocumentRepository.existsByGoalIdAndCreatedDateBetween(goalId, start, end);
    }

    public boolean hasDocumentCreatedToday(Long userId) {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);
        return categoryDocumentRepository.existsByGoal_User_IdAndCreatedDateBetween(userId, start, end);
    }

    public CategoryDocument getDocumentById(Long documentId) {
        return categoryDocumentRepository.findById(documentId)
                .orElseThrow(() -> new BaseException(CategoryDocumentResponseCode.NOT_FOUND_CATEGORY_DOCUMENT));
    }

    public CategoryDocument getDocumentWithCategoryById(Long documentId) {
        return categoryDocumentRepository.findWithCategoryAndGoalById(documentId)
                .orElseThrow(() -> new BaseException(CategoryDocumentResponseCode.NOT_FOUND_CATEGORY_DOCUMENT));
    }

    @Transactional
    public CategoryDocument createDocument(Goal goal, String content, String title) {
        CategoryDocument document = CategoryDocument.builder()
                .category(goal.getCategory())
                .goal(goal)
                .content(content)
                .title(title)
                .build();
        return categoryDocumentRepository.save(document);
    }

    @Transactional
    public void saveDocument(CategoryDocument document) {
        categoryDocumentRepository.saveAndFlush(document);
    }

    @Transactional
    public void deleteDocument(Long documentId) {
        categoryDocumentRepository.deleteById(documentId);
    }
}
