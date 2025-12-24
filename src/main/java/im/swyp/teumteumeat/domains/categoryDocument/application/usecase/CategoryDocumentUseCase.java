package im.swyp.teumteumeat.domains.categoryDocument.application.usecase;

import im.swyp.teumteumeat.domains.category.domain.service.CategoryService;
import im.swyp.teumteumeat.domains.category.persistence.entity.Category;
import im.swyp.teumteumeat.domains.categoryDocument.application.dto.response.CategoryDocumentResponse;
import im.swyp.teumteumeat.domains.categoryDocument.domain.service.CategoryDocumentService;
import im.swyp.teumteumeat.domains.categoryDocument.persistence.entity.CategoryDocument;
import im.swyp.teumteumeat.domains.goal.domain.service.GoalService;

import im.swyp.teumteumeat.domains.llm.domain.prompt.DocumentPrompt;
import im.swyp.teumteumeat.domains.llm.domain.service.LLMService;
import im.swyp.teumteumeat.domains.userQuiz.domain.service.UserQuizService;

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
    private final LLMService llmService;
    private final UserQuizService userQuizService;
    private final GoalService goalService;

    @Transactional
    public List<CategoryDocumentResponse> getDocuments(Long categoryId, Long userId) {
        List<CategoryDocument> documents = categoryDocumentService.getDocumentsByCategoryId(categoryId);
        // 1. 유저가 이미 학습(퀴즈 풀이)한 문서 ID 목록 조회
        List<Long> consumedDocumentIds = userQuizService.getConsumedDocumentIds(userId);

        // 유저가 해당 카테고리에서 본 적 없는 카테고리 자료들을 조회
        List<CategoryDocument> unconsumedDocuments = documents.stream()
                .filter(doc -> !consumedDocumentIds.contains(doc.getId()))
                .toList();

        // 유저가 해당 카테고리에서 카테고리 자료를 모두 소비했을 때
        if (unconsumedDocuments.isEmpty()) {
            // 카테고리 자료 생성
            CategoryDocument createdDocument = createDocumentInternal(categoryId, userId);
            unconsumedDocuments = List.of(createdDocument);
        }

        return unconsumedDocuments.stream()
                .map(CategoryDocumentResponse::from)
                .toList();
    }

    @Transactional
    public void createDocument(Long categoryId, Long userId) {
        createDocumentInternal(categoryId, userId);
    }

    private CategoryDocument createDocumentInternal(Long categoryId, Long userId) {
        Category category = categoryService.getCategoryById(categoryId);

        String topicInstruction = goalService.getTopic(userId, categoryId);

        // LLM을 통해 콘텐츠 생성 (Goal의 prompt 반영)
        String prompt = String.format(DocumentPrompt.GENERATE_DOCUMENT.getTemplate(), category.getName(),
                topicInstruction);
        String content = llmService.generateContent(prompt);

        CategoryDocument document = CategoryDocument.builder()
                .category(category)
                .content(content)
                .build();

        categoryDocumentService.saveDocument(document);
        return document;
    }

    @Transactional
    public void deleteDocument(Long documentId) {
        categoryDocumentService.deleteDocument(documentId);
    }
}
