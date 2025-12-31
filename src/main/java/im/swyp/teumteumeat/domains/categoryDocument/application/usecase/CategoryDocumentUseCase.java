package im.swyp.teumteumeat.domains.categoryDocument.application.usecase;

import im.swyp.teumteumeat.domains.category.domain.service.CategoryService;
import im.swyp.teumteumeat.domains.category.persistence.entity.Category;
import im.swyp.teumteumeat.domains.categoryDocument.application.dto.response.CategoryDocumentResponse;
import im.swyp.teumteumeat.domains.categoryDocument.domain.service.CategoryDocumentService;
import im.swyp.teumteumeat.domains.categoryDocument.persistence.entity.CategoryDocument;
import im.swyp.teumteumeat.domains.goal.domain.service.GoalService;
import im.swyp.teumteumeat.domains.goal.persistence.entity.Goal;

import im.swyp.teumteumeat.domains.llm.domain.prompt.DocumentPrompt;
import im.swyp.teumteumeat.domains.user.domain.service.UserService;
import im.swyp.teumteumeat.domains.llm.domain.service.LLMService;

import im.swyp.teumteumeat.global.annotation.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import im.swyp.teumteumeat.global.exception.BaseException;
import im.swyp.teumteumeat.domains.goal.domain.constant.GoalResponseCode;
import im.swyp.teumteumeat.domains.quiz.domain.constant.QuizResponseCode;
import im.swyp.teumteumeat.global.common.CommonResponseCode;

import java.time.LocalDate;
import java.util.List;

@UseCase
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryDocumentUseCase {

    private final CategoryDocumentService categoryDocumentService;
    private final UserService userService;
    private final UserQuizService userQuizService;
    private final GoalService goalService;
    private final LLMService llmService;

    @Transactional
    public CategoryDocumentResponse generateDocument(Long categoryId, Long userId) {
        // 0. Goal 및 일일 퀴즈 풀이 여부 확인
        Goal goal = goalService.findLatestGoal(userId, categoryId);
        if (goal.getEndDate().isBefore(LocalDate.now())) {
            throw new BaseException(GoalResponseCode.GOAL_EXPIRED);
        }

        if (userQuizService.hasSolvedQuizToday(userId, categoryId)) {
            throw new BaseException(QuizResponseCode.TODAY_QUOTA_EXCEEDED);
        }

        List<CategoryDocument> documents = categoryDocumentService.getDocumentsByGoalId(goal.getId());
        // 1. 유저가 이미 학습(퀴즈 풀이)한 문서 ID 목록 조회
        List<Long> consumedDocumentIds = userQuizService.getConsumedDocumentIds(userId);

        // 유저가 해당 카테고리에서 본 적 없는 카테고리 자료들을 조회
        List<CategoryDocument> unconsumedDocuments = documents.stream()
                .filter(doc -> !consumedDocumentIds.contains(doc.getId()))
                .toList();

        CategoryDocument targetDocument;

        // 유저가 해당 카테고리에서 카테고리 자료를 모두 소비했을 때 (또는 없을 때)
        if (unconsumedDocuments.isEmpty()) {
            // 카테고리 자료 생성
            targetDocument = createDocumentInternal(goal);
        } else {
            // 안 본 것 중 하나 선택 (첫 번째)
            targetDocument = unconsumedDocuments.get(0);
        }

        return CategoryDocumentResponse.from(targetDocument);
    }

    @Transactional(readOnly = true)
    public CategoryDocumentResponse getDailyDocument(Long categoryId, Long userId) {
        Goal goal = goalService.findLatestGoal(userId, categoryId);
        if (goal.getEndDate().isBefore(LocalDate.now())) {
            throw new BaseException(GoalResponseCode.GOAL_EXPIRED);
        }

        if (userQuizService.hasSolvedQuizToday(userId, categoryId)) {
            // 이미 풀었다면 히스토리 이용 유도
            throw new BaseException(QuizResponseCode.TODAY_QUOTA_EXCEEDED);
        }

        List<CategoryDocument> documents = categoryDocumentService.getDocumentsByGoalId(goal.getId());
        List<Long> consumedDocumentIds = userQuizService.getConsumedDocumentIds(userId);

        CategoryDocument targetDocument = documents.stream()
                .filter(doc -> !consumedDocumentIds.contains(doc.getId()))
                .findFirst()
                .orElseThrow(() -> new BaseException(CommonResponseCode.NOT_FOUND));

        return CategoryDocumentResponse.from(targetDocument);
    }

    @Transactional
    public void createDocument(Long categoryId, Long userId) {
        Goal goal = goalService.findLatestGoal(userId, categoryId);
        createDocumentInternal(goal);
    }

    private CategoryDocument createDocumentInternal(Goal goal) {
        Category category = goal.getCategory();
        String prompt = goal.getPrompt();
        String topicInstruction = (prompt != null && !prompt.isEmpty()) ? prompt : "전반적인 내용";

        // LLM을 통해 콘텐츠 생성
        String llmPrompt = String.format(DocumentPrompt.GENERATE_DOCUMENT.getTemplate(), category.getName(),
                topicInstruction);
        String content = llmService.generateContent(llmPrompt);

        // 제목 생성
        String generatedTitle = llmService.generateTitle(content, topicInstruction);

        CategoryDocument document = CategoryDocument.builder()
                .category(category)
                .goal(goal)
                .content(content)
                .title(generatedTitle)
                .build();

        categoryDocumentService.saveDocument(document);
        return document;
    }

    @Transactional
    public void deleteDocument(Long documentId) {
        categoryDocumentService.deleteDocument(documentId);
    }
}
