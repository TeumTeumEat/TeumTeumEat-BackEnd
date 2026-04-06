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
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@UseCase
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryDocumentUseCase {

    private final CategoryDocumentService categoryDocumentService;
    private final UserService userService;
    private final UserQuizService userQuizService;
    private final LLMService llmService;
    private final DistributedLockFacade distributedLockFacade;

    // (User) 요약글 생성
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public CategoryDocumentResponse generateDocument(Long categoryId, Long userId) {
        Goal goal = getValidGoal(userId, categoryId);
        Category category = goal.getCategory();
        checkUnsolvedQuota(goal, userId);

        String lockKey = "lock:category_document:generation:" + goal.getId();
        // 반환값을 직접 받기
        CategoryDocument targetDocument = distributedLockFacade.tryExecuteWithLock(
                lockKey, 30, 60, TimeUnit.SECONDS,
                () -> {
                    String llmPrompt = createLLMPrompt(goal, category);
                    return createDocumentInternal(goal, category, llmPrompt);
                }
        ).orElseThrow(() -> new BaseException(CommonResponseCode.INTERNAL_SERVER_ERROR));

        boolean isFirstTime = !userQuizService.hasSolvedAnyQuizEver(userId);

        return CategoryDocumentResponse.from(targetDocument, false, isFirstTime);
    }

    // 비동기식 요약글 생성 (스트리밍)
    public SseEmitter generateDocumentStream(Long categoryId, Long userId) {
        SseEmitter sseEmitter = new SseEmitter(180_000L);
        try {
            // 프론트에게 스트리밍 성공 이벤트 반환
            sseEmitter.send(SseEmitter.event().name("CONNECT").data("Stream 연결"));

            Goal goal = getValidGoal(userId, categoryId);
            checkUnsolvedQuota(goal, userId);

            StringBuilder generatedContent = new StringBuilder();

            // 프롬프트 생성 로직
            Category category = goal.getCategory();
            String llmPrompt = createLLMPrompt(goal, category);
            String topicInstruction = processUserPrompt(goal);

            // 한 글자씩 sse event로 전송
            llmService.generateContentStream(llmPrompt)
                    .subscribe(
                            parsedText -> {
                                try {
                                    sseEmitter.send(SseEmitter.event().name("message").data(parsedText));
                                    generatedContent.append(parsedText);
                                } catch (IOException e) {
                                    sseEmitter.completeWithError(e);
                                }
                            },
                            error -> {
                                log.error("LLM Stream Error: ", error);
                                sseEmitter.completeWithError(error);
                            },
                            () -> {
                                // 비동기로 제목 생성 및 DB 저장 (스트리밍용 스레드 블로킹 방지)
                                CompletableFuture.runAsync(() -> {
                                            categoryDocumentService.generateTitleandSaveDocument(category, goal, topicInstruction, generatedContent.toString());
                                        })
                                        .thenRun(() -> {
                                            // 비동기 작업이 끝난 후 스트림 종료
                                            sseEmitter.complete();
                                        })
                                        .exceptionally(e -> {
                                            log.error("문서 저장 중 에러 발생!", e);
                                            sseEmitter.completeWithError(e);
                                            return null; // 에러 핸들링
                                        });
                            }

                    );
        } catch (Exception e) {
            sseEmitter.completeWithError(e);
        }

        return sseEmitter;
    }

    // (User) 요약글 조회
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
        Category category = goal.getCategory();

        // 프롬프트 생성 로직
        String llmPrompt = createLLMPrompt(goal, category);
        createDocumentInternal(goal, category, llmPrompt);
    }

    // 목표 검증
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

    // 공용 요약글 재사용
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

    // 요약글 생성 내부 로직
    private CategoryDocument createDocumentInternal(Goal goal, Category category, String llmPrompt) {
        // CategoryDocument 생성
        String content = llmService.generateContent(llmPrompt);
        // LLM이 길게 생성할 경우를 대비하여 길이 제한 (공백 포함 600자) - 문장 단위로 자르기
        content = ContentUtils.truncateContentSafe(content);

        // 제목 생성
        String topicInstruction = processUserPrompt(goal);
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

    // LLM Prompt 생성
    private String createLLMPrompt(Goal goal, Category category) {
        String topicInstruction = processUserPrompt(goal);

        String path = category.getPath();
        String description = category.getDescription() != null ? category.getDescription()
                : path + " " + category.getName();

        String llmPrompt = String.format(DocumentPrompt.GENERATE_DOCUMENT.getTemplate(), category.getName(),
                path, description, topicInstruction);

        return llmPrompt;
    }

    private void checkUnsolvedQuota(Goal goal, Long userId) {
        UserEntity user = userService.getUserById(userId);

        // 관리자는 쿼터 무제한
        if (user.getRole() == Role.ADMIN) {
            return;
        }
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

    private String processUserPrompt(Goal goal) {
        String prompt = goal.getPrompt();
        return (prompt != null && !prompt.isEmpty()) ? prompt : "전반적인 내용";
    }

    // 요약글 삭제
    @Transactional
    public void deleteDocument(Long documentId) {
        categoryDocumentService.deleteDocument(documentId);
    }
}
