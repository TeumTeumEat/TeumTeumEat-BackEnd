package im.swyp.teumteumeat.domains.categoryDocument.application.usecase;

import im.swyp.teumteumeat.domains.goal.persistence.entity.Goal;

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
        // 유저의 현재 Goal 조회
        Goal goal = goalService.findLatestGoal(userId, categoryId);

        // 1. Goal에 해당하는 개인화된 문서 조회
        List<CategoryDocument> documents = categoryDocumentService.getDocumentsByGoalId(goal.getId());

        // 2. 유저가 이미 학습(퀴즈 풀이)한 문서 ID 목록 조회
        List<Long> consumedDocumentIds = userQuizService.getConsumedDocumentIds(userId);

        // 3. 미소비 문서 필터링 (개인화 문서 중)
        List<CategoryDocument> unconsumedDocuments = documents.stream()
                .filter(doc -> !consumedDocumentIds.contains(doc.getId()))
                .toList();

        // 4. 개인화 문서가 없을 때 + Prompt가 없는(기본) 경우 -> 카테고리 내 다른 문서 재사용 시도
        if (unconsumedDocuments.isEmpty() && (goal.getPrompt() == null || goal.getPrompt().isBlank())) {
            List<CategoryDocument> allCategoryDocs = categoryDocumentService.getDocumentsByCategoryId(categoryId);
            unconsumedDocuments = allCategoryDocs.stream()
                    .filter(doc -> !consumedDocumentIds.contains(doc.getId()))
                    .filter(doc -> {
                        Goal docGoal = doc.getGoal();
                        // 1. Goal이 없는(공용) 문서
                        if (docGoal == null) {
                            return true;
                        }
                        // 2. Goal이 있지만 Prompt가 없는(기본) 문서
                        return docGoal.getPrompt() == null || docGoal.getPrompt().isBlank();
                    })
                    .limit(1)
                    .toList();
        }

        // 5. 여전히 없으면, 새로 생성 (내 Goal에 맞춰서)
        if (unconsumedDocuments.isEmpty()) {
            CategoryDocument createdDocument = createDocumentInternal(categoryId, userId, goal);
            unconsumedDocuments = List.of(createdDocument);
        }

        return unconsumedDocuments.stream()
                .map(CategoryDocumentResponse::from)
                .toList();
    }

    @Transactional
    public void createDocument(Long categoryId, Long userId) {
        Goal goal = goalService.findLatestGoal(userId, categoryId);
        createDocumentInternal(categoryId, userId, goal);
    }

    private CategoryDocument createDocumentInternal(Long categoryId, Long userId, Goal goal) {
        Category category = categoryService.getCategoryById(categoryId);

        String topicInstruction = goalService.getTopic(userId, categoryId);

        // LLM을 통해 콘텐츠 생성 (Goal의 prompt 반영)
        String prompt = String.format(DocumentPrompt.GENERATE_DOCUMENT.getTemplate(), category.getName(),
                topicInstruction);
        String content = llmService.generateContent(prompt);

        CategoryDocument document = CategoryDocument.builder()
                .category(category)
                .content(content)
                .title(topicInstruction.length() > 20 ? topicInstruction.substring(0, 20) : topicInstruction)
                .goal(goal)
                .build();

        categoryDocumentService.saveDocument(document);
        return document;
    }

    @Transactional
    public void deleteDocument(Long documentId) {
        categoryDocumentService.deleteDocument(documentId);
    }
}
