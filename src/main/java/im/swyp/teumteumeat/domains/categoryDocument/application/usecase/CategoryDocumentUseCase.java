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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
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
                try {
                    createDocumentInternal(goal);
                } catch (DataIntegrityViolationException e) {
                    // 이미 생성된 경우 무시하고 조회 진행
                }
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
        List<CategoryDocument> documents = new ArrayList<>(
                categoryDocumentService.getDocumentsByGoalId(goal.getId()));

        boolean isDefaultPrompt = goal.getPrompt() == null || goal.getPrompt().isBlank();
        if (isDefaultPrompt) {
            List<CategoryDocument> commonDocuments = categoryDocumentService
                    .getCommonDocuments(goal.getCategory().getId());
            documents.addAll(commonDocuments);
        }

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
        try {
            return createDocumentInternal(goal);
        } catch (DataIntegrityViolationException e) {
            // 동시성 이슈 등으로 이미 생성된 경우, 기존 문서 반환
            return categoryDocumentService.getDocumentsByGoalId(goal.getId()).stream()
                    .filter(d -> d.getCreatedDate().toLocalDate().isEqual(LocalDate.now()))
                    .findFirst()
                    .orElseThrow(() -> e);
        }
    }

    private CategoryDocument createDocumentInternal(Goal goal) {
        Category category = goal.getCategory();
        String prompt = goal.getPrompt();
        String topicInstruction = (prompt != null && !prompt.isEmpty()) ? prompt : "전반적인 내용";

        // LLM을 통해 콘텐츠 생성
        String llmPrompt = String.format(DocumentPrompt.GENERATE_DOCUMENT.getTemplate(), category.getName(),
                topicInstruction);
        String content = llmService.generateContent(llmPrompt);
        // LLM이 길게 생성할 경우를 대비하여 길이 제한 (공백 포함 600자)
        content = content.substring(0, Math.min(content.length(), 600));

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
