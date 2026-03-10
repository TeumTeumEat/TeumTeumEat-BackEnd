package im.swyp.teumteumeat.domains.categoryDocument.application.usecase;

import im.swyp.teumteumeat.domains.user.domain.constant.Role;
import im.swyp.teumteumeat.domains.user.persistence.entity.UserEntity;
import im.swyp.teumteumeat.domains.category.persistence.entity.Category;
import im.swyp.teumteumeat.domains.categoryDocument.application.dto.response.CategoryDocumentResponse;
import im.swyp.teumteumeat.domains.categoryDocument.domain.service.CategoryDocumentService;
import im.swyp.teumteumeat.domains.categoryDocument.persistence.entity.CategoryDocument;
import im.swyp.teumteumeat.domains.goal.domain.constant.GoalResponseCode;
import im.swyp.teumteumeat.domains.goal.persistence.entity.Goal;
import im.swyp.teumteumeat.domains.llm.domain.prompt.DocumentPrompt;
import im.swyp.teumteumeat.domains.llm.domain.service.LLMService;
import im.swyp.teumteumeat.domains.quiz.domain.constant.QuizResponseCode;
import im.swyp.teumteumeat.domains.user.domain.service.UserService;
import im.swyp.teumteumeat.domains.userQuiz.domain.service.UserQuizService;
import im.swyp.teumteumeat.global.annotation.UseCase;
import im.swyp.teumteumeat.global.common.CommonResponseCode;
import im.swyp.teumteumeat.global.exception.BaseException;
import im.swyp.teumteumeat.global.component.DistributedLockFacade;
import im.swyp.teumteumeat.global.util.ContentUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@UseCase
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryDocumentUseCase {

    private final CategoryDocumentService categoryDocumentService;
    private final UserService userService;
    private final UserQuizService userQuizService;
    private final LLMService llmService;
    private final DistributedLockFacade distributedLockFacade;

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public CategoryDocumentResponse generateDocument(Long categoryId, Long userId) {
        Goal goal = getValidGoal(userId, categoryId);
        UserEntity user = userService.getUserById(userId);

        if (user.getRole() != Role.ADMIN && !user.canSolveDailyQuiz()) {
            throw new BaseException(QuizResponseCode.TODAY_QUOTA_EXCEEDED);
        }

        // 새 문서 생성 (무조건)
        CategoryDocument targetDocument = createNewDailyDocument(goal);
        boolean isFirstTime = !userQuizService.hasSolvedAnyQuizEver(userId);

        return CategoryDocumentResponse.from(targetDocument, false, isFirstTime);
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public CategoryDocumentResponse getDailyDocument(Long categoryId, Long userId) {
        Goal goal = getValidGoal(userId, categoryId);
        UserEntity user = userService.getUserById(userId);

        boolean outOfQuota = !user.canSolveDailyQuiz();
        boolean isAdmin = user.getRole() == Role.ADMIN;
        boolean hasSolvedToday = !isAdmin && outOfQuota;
        boolean isFirstTime = !userQuizService.hasSolvedAnyQuizEver(userId);

        // 단순 조회: 푼 지 안 푼 지 모르는 유저의 활성화된 최신 문서를 조회
        CategoryDocument targetDocument = getNextDocument(goal, userId);

        if (targetDocument == null) {
            // 풀 수 있는 남은 안 푼 문서가 없다면 에러 반환 (대신 예외로 404를 치거나 빈 응답)
            // 오늘 생성된 최신 1개를 그냥 보여주기로 함
            targetDocument = categoryDocumentService.getDocumentsByGoalId(goal.getId()).stream()
                    .reduce((first, second) -> second)
                    .orElseThrow(() -> new BaseException(CommonResponseCode.NOT_FOUND));
        }

        return CategoryDocumentResponse.from(targetDocument, hasSolvedToday, isFirstTime);
    }

    @Transactional
    public List<CategoryDocumentResponse> getDocuments(Long categoryId, Long userId) {
        Goal goal = getValidGoal(userId, categoryId);
        UserEntity user = userService.getUserById(userId);

        List<CategoryDocument> documents = categoryDocumentService.getDocumentsByGoalId(goal.getId());
        boolean outOfQuota = !user.canSolveDailyQuiz();
        boolean hasSolvedToday = user.getRole() != Role.ADMIN && outOfQuota;
        boolean isFirstTime = !userQuizService.hasSolvedAnyQuizEver(userId);

        CategoryDocument nextDoc = getNextDocument(goal, userId);

        // 오늘 쿼타가 남아있고 안 풀은 문서가 없다면 -> 새로 생성
        if (!outOfQuota && nextDoc == null) {
            try {
                createNewDailyDocument(goal);
            } catch (Exception e) {
                // 이미 생성되었거나 락 획득 실패 등은 무시하고 조회 진행
            }
            documents = categoryDocumentService.getDocumentsByGoalId(goal.getId());
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
        Goal goal = userService.getUserWithCurrentGoal(userId).getCurrentGoal();
        if (goal == null || !goal.getCategory().getId().equals(categoryId)) {
            throw new BaseException(CommonResponseCode.NOT_FOUND);
        }
        if (goal.getEndDate().isBefore(LocalDate.now())) {
            throw new BaseException(GoalResponseCode.GOAL_EXPIRED);
        }
        return goal;
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
        String lockKey = "lock:category_document:generation:" + goal.getId();

        // 30초 대기: LLM 생성이 오래 걸리므로 대기 시간 확보
        return distributedLockFacade.tryExecuteWithLock(lockKey, 30, 60, TimeUnit.SECONDS, () -> {
            // 무조건 생성
            return createDocumentInternal(goal);
        }).orElseThrow(() -> new BaseException(CommonResponseCode.INTERNAL_SERVER_ERROR));
    }

    private CategoryDocument createDocumentInternal(Goal goal) {
        Category category = goal.getCategory();
        String prompt = goal.getPrompt();
        String topicInstruction = (prompt != null && !prompt.isEmpty()) ? prompt : "전반적인 내용";

        // LLM을 통해 콘텐츠 생성
        String path = category.getPath();
        String description = category.getDescription() != null ? category.getDescription()
                : path + " " + category.getName();

        String llmPrompt = String.format(DocumentPrompt.GENERATE_DOCUMENT.getTemplate(), category.getName(),
                path, description, topicInstruction);
        String content = llmService.generateContent(llmPrompt);
        // LLM이 길게 생성할 경우를 대비하여 길이 제한 (공백 포함 600자) - 문장 단위로 자르기
        content = ContentUtils.truncateContentSafe(content);

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
