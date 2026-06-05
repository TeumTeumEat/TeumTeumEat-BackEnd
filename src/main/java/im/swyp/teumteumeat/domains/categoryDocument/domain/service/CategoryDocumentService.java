package im.swyp.teumteumeat.domains.categoryDocument.domain.service;

import im.swyp.teumteumeat.domains.category.persistence.entity.Category;
import im.swyp.teumteumeat.domains.categoryDocument.domain.constant.CategoryDocumentResponseCode;
import im.swyp.teumteumeat.domains.categoryDocument.persistence.entity.CategoryDocument;
import im.swyp.teumteumeat.domains.categorySubtopic.persistence.entity.CategorySubtopic;
import im.swyp.teumteumeat.domains.goal.persistence.entity.Goal;
import im.swyp.teumteumeat.domains.categoryDocument.persistence.repository.CategoryDocumentRepository;
import im.swyp.teumteumeat.domains.llm.domain.service.LLMService;
import im.swyp.teumteumeat.global.exception.BaseException;
import im.swyp.teumteumeat.global.util.ContentUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryDocumentService {

    private final CategoryDocumentRepository categoryDocumentRepository;

    private final LLMService llmService;

    public Optional<CategoryDocument> getLatestDocumentByGoalId(Long goalId) {
        return categoryDocumentRepository.findTopByGoal_IdOrderByCreatedDateDesc(goalId);
    }

    public Optional<CategoryDocument> getDocumentByGoalAndSubtopic(Long goalId, Long categorySubtopicId) {
        return categoryDocumentRepository.findTopByGoal_IdAndCategorySubtopic_IdOrderByCreatedDateDesc(goalId, categorySubtopicId);
    }

    public List<CategoryDocument> getCommonDocuments(Long categoryId) {
        return categoryDocumentRepository.findAllByCategoryIdAndGoalIsNull(categoryId);
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
    public CategoryDocument generateTitleandSaveDocument(Category category, Goal goal, String topicInstruction, String content, CategorySubtopic categorySubtopic) {
        // LLM이 길게 생성할 경우를 대비하여 길이 제한 (공백 포함 600자) - 문장 단위로 자르기
        String safeContent = ContentUtils.truncateContentSafe(content);

        String generatedTitle = llmService.generateTitle(safeContent, topicInstruction);

        CategoryDocument document = CategoryDocument.builder()
                .category(category)
                .goal(goal)
                .content(safeContent)
                .title(generatedTitle)
                .categorySubtopic(categorySubtopic)
                .build();

        saveDocument(document);

        return document;
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
