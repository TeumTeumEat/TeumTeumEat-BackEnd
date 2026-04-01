package im.swyp.teumteumeat.domains.categoryDocument.application.usecase;

import im.swyp.teumteumeat.domains.category.domain.service.CategoryService;
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
import im.swyp.teumteumeat.global.sse.service.NotificationService;
import im.swyp.teumteumeat.global.util.ContentUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@UseCase
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryDocumentUseCase {

    private final CategoryDocumentService categoryDocumentService;
    private final UserService userService;
    private final UserQuizService userQuizService;
    private final CategoryService categoryService;
    private final LLMService llmService;
    private final DistributedLockFacade distributedLockFacade;
    private final NotificationService notificationService;
    private final ApplicationEventPublisher eventPublisher;

    public SseEmitter subscribe(Long userId, Long categoryId, String lastEventId) {
        categoryService.getCategoryById(categoryId);

        return notificationService.subscribe(
                lastEventId,
                (dto) -> {
                    String eventId = dto.id() + ":" + System.currentTimeMillis();
                    notificationService.sendToTarget(dto.emitter(), dto.id(), eventId, "DOCUMENT_PROCESSING", "안내서 생성을 진행 중입니다.");
                },
                userId,
                categoryId);
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public CategoryDocumentResponse generateDocument(Long categoryId, Long userId) {
        Goal goal = getValidGoal(userId, categoryId);
        UserEntity user = userService.getUserById(userId);

        if (user.getRole() != Role.ADMIN && !user.canSolveDailyQuiz()) {
            throw new BaseException(QuizResponseCode.TODAY_QUOTA_EXCEEDED);
        }

        // 1. 이미 사용자에게 할당되어 있으나, 아직 퀴즈를 풀지 않은 "최신/활성" 문서가 있는지 확인
        if (user.getRole() != Role.ADMIN) {
            CategoryDocument activeDocument = getCurrentActiveDocument(goal, userId);
            if (activeDocument != null) {
                boolean isConsumed = userQuizService.getConsumedDocumentIds(userId).contains(activeDocument.getId());
                if (!isConsumed) {
                    throw new BaseException(QuizResponseCode.UNSOLVED_QUIZ_EXISTS);
                }
            }
        }

        // 새 문서 생성 (무조건)
        CategoryDocument targetDocument = createNewDailyDocument(goal);
        boolean isFirstTime = !userQuizService.hasSolvedAnyQuizEver(userId);

        return CategoryDocumentResponse.from(targetDocument, false, isFirstTime);
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public CategoryDocumentResponse generateDocumentAsync(Long categoryId, Long userId) {
        Goal goal = getValidGoal(userId, categoryId);
        UserEntity user = userService.getUserById(userId);

        if (user.getRole() != Role.ADMIN && !user.canSolveDailyQuiz()) {
            throw new BaseException(QuizResponseCode.TODAY_QUOTA_EXCEEDED);
        }

        // 1. 이미 사용자에게 할당되어 있으나, 아직 퀴즈를 풀지 않은 "최신/활성" 문서가 있는지 확인
        if (user.getRole() != Role.ADMIN) {
            CategoryDocument activeDocument = getCurrentActiveDocument(goal, userId);
            if (activeDocument != null) {
                boolean isConsumed = userQuizService.getConsumedDocumentIds(userId).contains(activeDocument.getId());
                if (!isConsumed) {
                    throw new BaseException(QuizResponseCode.UNSOLVED_QUIZ_EXISTS);
                }
            }
        }

        boolean isFirstTime = !userQuizService.hasSolvedAnyQuizEver(userId);
        
        // 요약글 생성 이벤트 publish
        eventPublisher.publishEvent(new CategoryDocumentGenerationEvent(categoryId, userId, goal, isFirstTime));
    }

    @Async
    @EventListener
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void handleDocumentGenerationEvent(CategoryDocumentGenerationEvent event) {
        String key = notificationService.generateKey(event.userId(), event.categoryId());
        try {
            // 요약글 생성
            CategoryDocument targetDocument = createNewDailyDocument(event.goal());
            CategoryDocumentResponse response = CategoryDocumentResponse.from(targetDocument, false, event.isFirstTime());
            
            notificationService.send(key, "DOCUMENT_GENERATED", response);
        } catch (Exception e) {
            log.error("카테고리 요약글 생성 실패 userId: {}", event.userId(), e);
            notificationService.send(key, "GENERATION_ERROR", "요약글 생성에 실패했습니다.");
        }
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public CategoryDocumentResponse getDailyDocument(Long categoryId, Long userId) {
        Goal goal = getValidGoal(userId, categoryId);
        UserEntity user = userService.getUserById(userId);

        boolean outOfQuota = !user.canSolveDailyQuiz();
        boolean isAdmin = user.getRole() == Role.ADMIN;
        boolean hasSolvedToday = !isAdmin && outOfQuota;
        boolean isFirstTime = !userQuizService.hasSolvedAnyQuizEver(userId);

        // 단순 조회: 가장 최근에 발급받은(생성된) 문서를 우선 반환
        CategoryDocument targetDocument = getCurrentActiveDocument(goal, userId);

        if (targetDocument == null) {
            throw new BaseException(CommonResponseCode.NOT_FOUND);
        }

        return CategoryDocumentResponse.from(targetDocument, hasSolvedToday, isFirstTime);
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
        if (goal.isCompleted()) {
            throw new BaseException(GoalResponseCode.GOAL_COMPLETED);
        }
        if (goal.getEndDate().isBefore(LocalDate.now())) {
            throw new BaseException(GoalResponseCode.GOAL_EXPIRED);
        }
        return goal;
    }

    private CategoryDocument getCurrentActiveDocument(Goal goal, Long userId) {
        // 1. 유저가 직접 생성한 최신 문서가 있는지 확인
        Optional<CategoryDocument> latestDocOpt = categoryDocumentService.getLatestDocumentByGoalId(goal.getId());
        if (latestDocOpt.isPresent()) {
            return latestDocOpt.get(); // 가장 최근 생성 문서를 우선으로
        }

        // 2. 만약 직접 생성한 문서가 없다면, 초기 학습 상태이므로 공용 문서 중 아직 안 푼 가장 첫 번째 문서를 찾음
        boolean isDefaultPrompt = goal.getPrompt() == null || goal.getPrompt().isBlank();
        if (isDefaultPrompt) {
            List<CategoryDocument> commonDocuments = categoryDocumentService
                    .getCommonDocuments(goal.getCategory().getId());
            List<Long> consumedDocumentIds = userQuizService.getConsumedDocumentIds(userId);

            return commonDocuments.stream()
                    .filter(doc -> !consumedDocumentIds.contains(doc.getId()))
                    .findFirst()
                    .orElse(null);
        }

        return null;
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
