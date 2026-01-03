package im.swyp.teumteumeat.domains.categoryDocument.application.usecase;

import im.swyp.teumteumeat.domains.category.persistence.entity.Category;
import im.swyp.teumteumeat.domains.categoryDocument.application.dto.response.CategoryDocumentResponse;
import im.swyp.teumteumeat.domains.categoryDocument.domain.service.CategoryDocumentService;
import im.swyp.teumteumeat.domains.categoryDocument.persistence.entity.CategoryDocument;
import im.swyp.teumteumeat.domains.goal.domain.constant.GoalResponseCode;
import im.swyp.teumteumeat.domains.goal.domain.service.GoalService;
import im.swyp.teumteumeat.domains.goal.persistence.entity.Goal;
import im.swyp.teumteumeat.domains.llm.domain.prompt.DocumentPrompt;
import im.swyp.teumteumeat.domains.llm.domain.service.LLMService;
import im.swyp.teumteumeat.domains.quiz.domain.constant.QuizResponseCode;
import im.swyp.teumteumeat.domains.user.domain.service.UserService;
import im.swyp.teumteumeat.domains.userQuiz.domain.service.UserQuizService;
import im.swyp.teumteumeat.global.annotation.UseCase;
import im.swyp.teumteumeat.global.common.CommonResponseCode;
import im.swyp.teumteumeat.global.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

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
        Goal goal = getValidGoal(userId, categoryId);

        if (userQuizService.hasSolvedQuizToday(userId, categoryId)) {
            throw new BaseException(QuizResponseCode.TODAY_QUOTA_EXCEEDED);
        }

        CategoryDocument targetDocument = getNextDocumentOrCreate(goal, userId);
        boolean isFirstTime = !userQuizService.hasSolvedAnyQuizEver(userId);

        return CategoryDocumentResponse.from(targetDocument, false, isFirstTime);
    }

    @Transactional
    public CategoryDocumentResponse getDailyDocument(Long categoryId, Long userId) {
        Goal goal = getValidGoal(userId, categoryId);

        boolean hasSolvedToday = userQuizService.hasSolvedQuizToday(userId, categoryId);
        boolean isFirstTime = !userQuizService.hasSolvedAnyQuizEver(userId);

        CategoryDocument targetDocument = getNextDocument(goal, userId);

        if (targetDocument == null) {
            // 아직 오늘의 요약글이 없고, 오늘 퀴즈도 푼 적 없다면 -> 새로 생성
            if (!hasSolvedToday) {
                targetDocument = createNewDailyDocument(goal);
            } else {
                // 이미 오늘 문제를 풀었으면, 오늘 생성된 문서를 반환 (단순 조회용)
                targetDocument = categoryDocumentService.getDocumentsByGoalId(goal.getId()).stream()
                        .filter(d -> d.getCreatedDate().toLocalDate().isEqual(LocalDate.now()))
                        .findFirst()
                        .orElseThrow(() -> new BaseException(CommonResponseCode.NOT_FOUND));
            }
        }

        return CategoryDocumentResponse.from(targetDocument, hasSolvedToday, isFirstTime);
    }

    @Transactional
    public List<CategoryDocumentResponse> getDocuments(Long categoryId, Long userId) {
        Goal goal = getValidGoal(userId, categoryId);

        List<CategoryDocument> documents = categoryDocumentService.getDocumentsByGoalId(goal.getId());
        boolean hasSolvedToday = userQuizService.hasSolvedQuizToday(userId, categoryId);
        boolean isFirstTime = !userQuizService.hasSolvedAnyQuizEver(userId);

        boolean hasTodayDocument = !documents.isEmpty() &&
                documents.get(documents.size() - 1).getCreatedDate().toLocalDate().isEqual(LocalDate.now());

        // 오늘 생성된 자료가 없고, 오늘 퀴즈도 푼 적 없다면 -> 생성
        if (!hasSolvedToday && !hasTodayDocument) {
            if (!categoryDocumentService.existsByGoalIdAndDate(goal.getId(), LocalDate.now())) {
                createDocumentInternal(goal);
                documents = categoryDocumentService.getDocumentsByGoalId(goal.getId());
            }
        }

        return documents.stream()
                .map(doc -> CategoryDocumentResponse.from(doc, hasSolvedToday, isFirstTime))
                .toList();
    }

    @Transactional
    public void createDocument(Long categoryId, Long userId) {
        Goal goal = getValidGoal(userId, categoryId);
        createDocumentInternal(goal);
    }

    private Goal getValidGoal(Long userId, Long categoryId) {
        Goal goal = userService.getUserById(userId).getCurrentGoal();
        if (goal == null || !goal.getCategory().getId().equals(categoryId)) {
            throw new BaseException(CommonResponseCode.NOT_FOUND);
        }
        if (goal.getEndDate().isBefore(LocalDate.now())) {
            throw new BaseException(GoalResponseCode.GOAL_EXPIRED);
        }
        return goal;
    }

    private CategoryDocument getNextDocumentOrCreate(Goal goal, Long userId) {
        CategoryDocument targetDocument = getNextDocument(goal, userId);
        if (targetDocument == null) {
            targetDocument = createNewDailyDocument(goal);
        }
        return targetDocument;
    }

    private CategoryDocument getNextDocument(Goal goal, Long userId) {
        List<CategoryDocument> documents = categoryDocumentService.getDocumentsByGoalId(goal.getId());
        List<Long> consumedDocumentIds = userQuizService.getConsumedDocumentIds(userId);

        return documents.stream()
                .filter(doc -> !consumedDocumentIds.contains(doc.getId()))
                .findFirst()
                .orElse(null);
    }

    private CategoryDocument createNewDailyDocument(Goal goal) {
        if (categoryDocumentService.existsByGoalIdAndDate(goal.getId(), LocalDate.now())) {
            throw new BaseException(CommonResponseCode.NOT_FOUND);
        }
        return createDocumentInternal(goal);
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
