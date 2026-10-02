package im.swyp.teumteumeat.domains.categoryDocument.application.usecase;

import im.swyp.teumteumeat.domains.llm.application.component.LlmGenerationTemplate;
import im.swyp.teumteumeat.domains.user.persistence.entity.UserEntity;
import im.swyp.teumteumeat.domains.category.persistence.entity.Category;
import im.swyp.teumteumeat.domains.categoryDocument.application.dto.response.CategoryDocumentResponse;
import im.swyp.teumteumeat.domains.categoryDocument.domain.service.CategoryDocumentService;
import im.swyp.teumteumeat.domains.categoryDocument.persistence.entity.CategoryDocument;
import im.swyp.teumteumeat.domains.categorySubtopic.domain.service.CategorySubtopicService;
import im.swyp.teumteumeat.domains.categorySubtopic.persistence.entity.CategorySubtopic;
import im.swyp.teumteumeat.domains.goal.domain.constant.GoalResponseCode;
import im.swyp.teumteumeat.domains.goal.domain.constant.GoalType;
import im.swyp.teumteumeat.domains.goal.persistence.entity.Goal;
import im.swyp.teumteumeat.domains.llm.domain.prompt.DocumentPrompt;
import im.swyp.teumteumeat.domains.llm.domain.service.LLMService;
import im.swyp.teumteumeat.domains.quiz.application.usecase.QuizUseCase;
import im.swyp.teumteumeat.domains.quiz.domain.constant.QuizResponseCode;
import im.swyp.teumteumeat.domains.user.domain.service.UserService;
import im.swyp.teumteumeat.domains.userQuiz.domain.service.UserQuizService;
import im.swyp.teumteumeat.global.annotation.UseCase;
import im.swyp.teumteumeat.global.common.CommonResponseCode;
import im.swyp.teumteumeat.global.exception.BaseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
@UseCase
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryDocumentUseCase {

    private final CategoryDocumentService categoryDocumentService;
    private final UserService userService;
    private final UserQuizService userQuizService;
    private final LLMService llmService;
    private final LlmGenerationTemplate llmGenerationTemplate;
    private final CategorySubtopicService categorySubtopicService;
    private final QuizUseCase quizUseCase;

    // (User) 요약글 생성
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public CategoryDocumentResponse generateDocument(Long categoryId, Long userId) {
        Goal goal = validateAndGetGoalWithQuota(userId, categoryId);
        Category category = goal.getCategory();

        Optional<CategorySubtopic> subtopic = resolveCurrentSubtopic(goal);
        String topicInstruction = toTopicInstruction(subtopic, goal);
        String llmPrompt = createLLMPrompt(category, topicInstruction);

        String lockKey = "lock:category_document:generation:" + goal.getId();

        CategoryDocument categoryDocument = llmGenerationTemplate.executeSyncSummary(
                lockKey,
                llmPrompt,
                (generatedContent) -> categoryDocumentService.generateTitleAndSaveDocument(category, goal, topicInstruction, generatedContent, subtopic.orElse(null)),
                null
        );

        // 사용자가 요약글을 읽는 동안 퀴즈(검증 포함)를 미리 준비해둔다. executeSyncSummary는 postAction을
        // 요청 스레드에서 그대로 블로킹 실행하므로, 여기서 직접 CompletableFuture로 분리해 응답 지연에 영향 없게 한다.
        CompletableFuture.runAsync(() -> prefetchQuizzes(categoryDocument.getId(), userId));

        boolean isFirstTime = !userQuizService.hasSolvedAnyQuizEver(userId);

        return CategoryDocumentResponse.from(categoryDocument, false, isFirstTime);
    }

    // Stream 분리 (템플릿 콜백 패턴)
    // 비동기식 요약글 생성 (스트리밍)
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public SseEmitter generateDocumentStream(Long categoryId, Long userId) {
        Goal goal = validateAndGetGoalWithQuota(userId, categoryId);
        Category category = goal.getCategory();

        // 프롬프트 생성 로직
        Optional<CategorySubtopic> subtopic = resolveCurrentSubtopic(goal);
        String topicInstruction = toTopicInstruction(subtopic, goal);
        String llmPrompt = createLLMPrompt(category, topicInstruction);

        String cooldownKey = "cooldown:category_document:generation:" + goal.getId();

        // 템플릿 콜백 함수
        return llmGenerationTemplate.executeStreamSummary(
                cooldownKey,
                llmPrompt,
                (generatedContent) -> categoryDocumentService.generateTitleAndSaveDocument(category, goal, topicInstruction, generatedContent, subtopic.orElse(null)),
                (savedDocument) -> savedDocument.getTitle(),
                (savedDocument) -> prefetchQuizzes(savedDocument.getId(), userId)
        );
    }

    // (Admin) 요약글 생성
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void createDocument(Long categoryId, Long userId) {
        Goal goal = getValidGoal(userId, categoryId);
        Category category = goal.getCategory();

        Optional<CategorySubtopic> subtopic = resolveCurrentSubtopic(goal);
        String topicInstruction = toTopicInstruction(subtopic, goal);
        String llmPrompt = createLLMPrompt(category, topicInstruction);

        String generatedContent = llmService.generateContent(llmPrompt);
        categoryDocumentService.generateTitleAndSaveDocument(category, goal, topicInstruction, generatedContent, subtopic.orElse(null));
    }

    // (User) 요약글 조회
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public CategoryDocumentResponse getDailyDocument(Long categoryId, Long userId) {
        Goal goal = getGoalForRead(userId, categoryId);
        UserEntity user = userService.getUserById(userId);

        boolean outOfQuota = !user.canSolveDailyQuiz();
        boolean hasSolvedToday = outOfQuota;
        boolean isFirstTime = !userQuizService.hasSolvedAnyQuizEver(userId);

        // 단순 조회: 가장 최근에 발급받은(생성된) 문서를 우선 반환
        CategoryDocument targetDocument = getCurrentActiveDocument(goal, userId);

        if (targetDocument == null) {
            throw new BaseException(CommonResponseCode.NOT_FOUND);
        }

        return CategoryDocumentResponse.from(targetDocument, hasSolvedToday, isFirstTime);
    }

    // 단순 조회용 (완료 및 만료 검사 없음)
    private Goal getGoalForRead(Long userId, Long categoryId) {
        Goal goal = userService.getUserWithCurrentGoal(userId).getCurrentGoal();
        if (goal == null || !goal.getCategory().getId().equals(categoryId)) {
            throw new BaseException(CommonResponseCode.NOT_FOUND);
        }
        return goal;
    }

    // 목표 검증 (생성 및 제출용)
    private Goal getValidGoal(Long userId, Long categoryId) {
        Goal goal = userService.getUserWithCurrentGoal(userId).getCurrentGoal();
        if (goal == null || !goal.getCategory().getId().equals(categoryId)) {
            throw new BaseException(CommonResponseCode.NOT_FOUND);
        }
        if (goal.isCompleted()) {
            throw new BaseException(GoalResponseCode.GOAL_COMPLETED);
        }
        return goal;
    }

    // 공용 요약글 재사용
    private CategoryDocument getCurrentActiveDocument(Goal goal, Long userId) {
        // 소주제 적용 목표: 현재 소주제에 해당하는 문서만 재활용 대상으로 한정
        Optional<CategorySubtopic> subtopic = resolveCurrentSubtopic(goal);
        if (subtopic.isPresent()) {
            return categoryDocumentService
                    .getDocumentByGoalAndSubtopic(goal.getId(), subtopic.get().getId())
                    .orElse(null);
        }

        // 소주제 없는 목표: 기존 로직
        // 1. 유저가 직접 생성한 최신 문서가 있는지 확인
        Optional<CategoryDocument> latestDocOpt = categoryDocumentService.getLatestDocumentByGoalId(goal.getId());
        if (latestDocOpt.isPresent()) {
            return latestDocOpt.get();
        }

        // 2. 초기 학습 상태 - 공용 문서 중 아직 안 푼 첫 번째 문서
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


    // LLM Prompt 생성
    private String createLLMPrompt(Category category, String topicInstruction) {
        String path = category.getPath();
        String description = category.getDescription() != null ? category.getDescription()
                : path + " " + category.getName();

        return String.format(DocumentPrompt.GENERATE_DOCUMENT.getTemplate(), category.getName(),
                path, description, topicInstruction);
    }

    private void checkUnsolvedQuota(Goal goal, Long userId) {
        UserEntity user = userService.getUserById(userId);
        // 오늘의 퀴즈 해결 가능 여부 확인
        if (!user.canSolveDailyQuiz()) {
            throw new BaseException(QuizResponseCode.TODAY_QUOTA_EXCEEDED);
        }
        // 안 푼 퀴즈가 존재하는지 검증
        CategoryDocument activeDocument = getCurrentActiveDocument(goal, userId);
        if (activeDocument != null) {
            boolean isConsumed = userQuizService.getConsumedDocumentIds(userId).contains(activeDocument.getId());
            if (!isConsumed) {
                throw new BaseException(QuizResponseCode.UNSOLVED_QUIZ_EXISTS);
            }
        }
    }

    // 퀴즈 풀이 여부 검증 및 Goal 반환
    private Goal validateAndGetGoalWithQuota(Long userId, Long categoryId) {
        Goal goal = getValidGoal(userId, categoryId);
        checkUnsolvedQuota(goal, userId);
        return goal;
    }

    /**
     * 목표 진행 상황에 맞는 소주제를 반환한다.
     * @param goal 목표
     * @return 소주제 (목표 종류가 Document이거나, 소주제가 시딩되어 있지 않으면 Optional.empty() 를 반환)
     */
    private Optional<CategorySubtopic> resolveCurrentSubtopic(Goal goal) {
        if (goal.getType() != GoalType.CATEGORY) {
            return Optional.empty();
        }
        int currentIndex = goal.getCompletedQuizSetCount();
        int durationWeeks = goal.getTargetQuizSetCount() / 7;
        return categorySubtopicService.findSubtopic(goal.getCategory().getId(), durationWeeks, currentIndex);
    }

    private String toTopicInstruction(Optional<CategorySubtopic> subtopic, Goal goal) {
        String userPrompt = goal.getPrompt();
        boolean hasUserPrompt = StringUtils.hasText(userPrompt);
        return subtopic
                .map(s -> hasUserPrompt ? s.getTitle() + " (" + userPrompt + ")" : s.getTitle())
                .orElse(hasUserPrompt ? userPrompt : "전반적인 내용");
    }

    // 요약글 생성 직후 백그라운드에서 퀴즈(사전 검증 포함)를 미리 만들어둔다.
    // QuizUseCase.ensureQuizzesAvailable에서 필요 개수만큼 퀴즈 생성 혹은 반환
    // 실패하더라도 사용자가 실제로 풀이를 시작할 때 같은 로직(잠금 포함)이 안전망으로 재시도한다.
    private void prefetchQuizzes(Long categoryDocumentId, Long userId) {
        try {
            int quizCount = quizUseCase.calculateQuestionCount(userId);
            quizUseCase.ensureQuizzesAvailable(categoryDocumentId, userId, quizCount);
        } catch (Exception e) {
            log.warn("퀴즈 프리페치 실패 (풀이 시작 시점에 재시도됨): categoryDocumentId={}, userId={}", categoryDocumentId, userId, e);
        }
    }

    // 요약글 삭제
    @Transactional
    public void deleteDocument(Long documentId) {
        categoryDocumentService.deleteDocument(documentId);
    }
}
