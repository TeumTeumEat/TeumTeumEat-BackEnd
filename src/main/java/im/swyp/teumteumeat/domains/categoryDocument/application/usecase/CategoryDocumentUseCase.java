package im.swyp.teumteumeat.domains.categoryDocument.application.usecase;

import im.swyp.teumteumeat.domains.category.domain.service.CategoryService;
import im.swyp.teumteumeat.domains.category.persistence.entity.Category;
import im.swyp.teumteumeat.domains.categoryDocument.application.dto.response.CategoryDocumentResponse;
import im.swyp.teumteumeat.domains.categoryDocument.domain.service.CategoryDocumentService;
import im.swyp.teumteumeat.domains.categoryDocument.persistence.entity.CategoryDocument;
import im.swyp.teumteumeat.domains.llm.application.usecase.LLMUseCase;
import im.swyp.teumteumeat.domains.quiz.persistence.repository.UserQuizHistoryRepository;
import im.swyp.teumteumeat.global.annotation.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@UseCase
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryDocumentUseCase {

    private final CategoryDocumentService categoryDocumentService;
    private final CategoryService categoryService;
    private final LLMUseCase llmUseCase;
    private final UserQuizHistoryRepository userQuizHistoryRepository;

    @Transactional
    public List<CategoryDocumentResponse> getDocuments(Long categoryId, Long userId) {
        List<CategoryDocument> documents = categoryDocumentService.getDocumentsByCategoryId(categoryId);
        List<Long> consumedDocumentIds = userQuizHistoryRepository.findConsumedDocumentIdsByUserId(userId);

        // 유저가 해당 카테고리에서 본 적 없는 카테고리 자료들을 조회
        List<CategoryDocument> unconsumedDocuments = documents.stream()
                .filter(doc -> !consumedDocumentIds.contains(doc.getId()))
                .toList();

        // 유저가 해당 카테고리에서 카테고리 자료를 모두 소비했을 때
        if (unconsumedDocuments.isEmpty()) {
            // 카테고리 자료 생성
            CategoryDocument createdDocument = createDocumentInternal(categoryId);
            unconsumedDocuments = List.of(createdDocument);
        }

        return unconsumedDocuments.stream()
                .map(CategoryDocumentResponse::from)
                .toList();
    }

    @Transactional
    public void createDocument(Long categoryId) {
        createDocumentInternal(categoryId);
    }

    private CategoryDocument createDocumentInternal(Long categoryId) {
        Category category = categoryService.getCategoryById(categoryId);

        // LLM을 통해 콘텐츠 생성
        String content = llmUseCase.generateDocumentContent(category.getName());

        CategoryDocument document = CategoryDocument.builder()
                .category(category)
                .content(content)
                .build();

        categoryDocumentService.createDocument(document);
        return document;
    }

    @Transactional
    public void deleteDocument(Long documentId) {
        categoryDocumentService.deleteDocument(documentId);
    }
}
