package im.swyp.teumteumeat.domains.document.application.usecase;

import im.swyp.teumteumeat.domains.document.application.dto.response.DocumentDetailResponse;
import im.swyp.teumteumeat.domains.document.domain.constant.DocumentResponseCode;
import im.swyp.teumteumeat.domains.document.application.mapper.DocumentMapper;
import im.swyp.teumteumeat.domains.document.domain.service.DocumentService;
import im.swyp.teumteumeat.domains.document.domain.service.DocumentSummaryService;
import im.swyp.teumteumeat.domains.document.persistence.entity.Document;
import im.swyp.teumteumeat.domains.document.persistence.entity.DocumentSummary;
import im.swyp.teumteumeat.domains.document.persistence.repository.DocumentSummaryRepository;
import im.swyp.teumteumeat.domains.goal.domain.constant.GoalResponseCode;
import im.swyp.teumteumeat.domains.goal.domain.service.GoalService;
import im.swyp.teumteumeat.domains.goal.persistence.entity.Goal;
import im.swyp.teumteumeat.domains.llm.domain.prompt.DocumentPrompt;
import im.swyp.teumteumeat.domains.llm.domain.service.LLMService;
import im.swyp.teumteumeat.domains.quiz.application.usecase.QuizUseCase;
import im.swyp.teumteumeat.domains.quiz.domain.constant.QuizResponseCode;
import im.swyp.teumteumeat.domains.quiz.domain.service.QuizService;
import im.swyp.teumteumeat.domains.quiz.persistence.entity.Quiz;
import im.swyp.teumteumeat.domains.userQuiz.domain.service.UserQuizService;
import im.swyp.teumteumeat.global.annotation.UseCase;
import im.swyp.teumteumeat.global.common.CommonResponseCode;
import im.swyp.teumteumeat.global.exception.BaseException;
import im.swyp.teumteumeat.domains.user.domain.service.UserService;
import im.swyp.teumteumeat.domains.user.persistence.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@UseCase
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentSummaryUseCase {

    private final GoalService goalService;
    private final UserService userService;
    private final UserQuizService userQuizService;
    private final DocumentService documentService;
    private final DocumentSummaryRepository documentSummaryRepository;
    private final DocumentSummaryService documentSummaryService;
    private final QuizUseCase quizUseCase;
    private final QuizService quizService;
    private final LLMService llmService;

    @Transactional
    public DocumentDetailResponse getSummaryForView(Long userId, Long goalId, Long documentId) {
        Goal goal = goalService.getGoalById(goalId);
        goal.validateOwner(userId);

        // 조회 시에는 완료되거나 만료된 목표여도 기존 데이터 조회를 허용합니다.

        boolean isFirstTime = !userQuizService.hasSolvedAnyQuizEver(userId);
        Document document = documentService.getDocumentById(documentId);
        document.validateOwner(userId);

        if (document.getRawContent() == null || document.getRawContent().trim().isEmpty()) {
            throw new BaseException(DocumentResponseCode.DOCUMENT_NOT_READY);
        }

        UserEntity user = userService.getUserById(userId);
        boolean outOfQuota = !user.canSolveDailyQuiz();
        boolean hasSolvedToday = outOfQuota;

        // 단순 조회 로직: 가장 최신의 요약글 찾아서 반환 (자동 생성 x)
        Optional<DocumentSummary> latestSummaryOpt = documentSummaryRepository.findLatestByDocumentId(documentId);
        DocumentSummary summary = latestSummaryOpt.orElseThrow(() -> new BaseException(CommonResponseCode.NOT_FOUND));

        return DocumentMapper.toDocumentDetailResponse(document, summary, hasSolvedToday, isFirstTime);
    }

    // 문서 요약 및 상세 조회 (학습 시 사용 되는 메서드)
    @Transactional
    public DocumentDetailResponse createSummary(Long userId, Long goalId, Long documentId) {
        Goal goal = goalService.getGoalById(goalId);
        goal.validateOwner(userId);

        validateGoal(goal);

        Document document = documentService.getDocumentById(documentId);
        document.validateOwner(userId);

        checkQuotaAndUnsolvedQuizzes(userId, documentId);

        String prompt = String.format(DocumentPrompt.GENERATE_PDF_SUMMARY.getTemplate(), document.getRawContent());
        String summaryContent = llmService.generateContent(prompt);
        DocumentSummary summary = documentSummaryService.generateTitleAndSaveSummary(documentId, summaryContent);

        quizUseCase.createQuizzesForPdfDocument(document, summary);

        boolean isFirstTime = !userQuizService.hasSolvedAnyQuizEver(userId);

        return DocumentMapper.toDocumentDetailResponse(document, summary, false, isFirstTime);
    }

    public SseEmitter createSummaryStream(Long userId, Long goalId, Long documentId) {
        Goal goal = goalService.getGoalById(goalId);
        goal.validateOwner(userId);

        validateGoal(goal);

        Document document = documentService.getDocumentById(documentId);
        document.validateOwner(userId);

        SseEmitter sseEmitter = new SseEmitter(10 * 60 * 1000L); // 10분 설정
        StringBuilder generatedContent = new StringBuilder();

        try {
            sseEmitter.send(SseEmitter.event().name("CONNECT").data("Stream 연결"));
            checkQuotaAndUnsolvedQuizzes(userId, documentId);

            String llmPrompt = String.format(DocumentPrompt.GENERATE_PDF_SUMMARY.getTemplate(),
                    document.getRawContent());

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
                                CompletableFuture.supplyAsync(() -> {
                                    return documentSummaryService.generateTitleAndSaveSummary(documentId,
                                            generatedContent.toString());
                                })
                                        .thenAccept(savedSummary -> {
                                            try {
                                                sseEmitter.send(
                                                        SseEmitter.event().name("title").data(savedSummary.getTitle()));
                                                sseEmitter.complete();
                                            } catch (IOException e) {
                                                sseEmitter.completeWithError(e);
                                            }
                                            // 퀴즈 생성은 SSE 종료 여부와 상관 없이 비동기
                                            CompletableFuture.runAsync(() -> {
                                                try {
                                                    log.info("백그라운드에서 퀴즈 생성 시작 (문서 ID: {})", documentId);
                                                    quizUseCase.createQuizzesForPdfDocument(document, savedSummary);
                                                    log.info("백그라운드 퀴즈 생성 완료 (문서 ID: {})", documentId);
                                                } catch (Exception e) {
                                                    log.error("비동기 퀴즈 생성 중 에러 (문서 ID: {})", documentId, e);
                                                }
                                            });
                                        })
                                        .exceptionally(e -> {
                                            log.error("문서 저장 중 에러 발생!", e);
                                            sseEmitter.completeWithError(e);
                                            return null; // 에러 핸들링
                                        });
                            });
        } catch (Exception e) {
            sseEmitter.completeWithError(e);
        }

        return sseEmitter;
    }

    private void validateGoal(Goal goal) {
        // Goal 달성 및 만료 확인
        if (goal.isCompleted()) {
            throw new BaseException(GoalResponseCode.GOAL_COMPLETED);
        }
        if (goal.getEndDate().isBefore(LocalDate.now())) {
            throw new BaseException(GoalResponseCode.GOAL_EXPIRED);
        }
    }

    private void checkQuotaAndUnsolvedQuizzes(Long userId, Long documentId) {
        UserEntity user = userService.getUserById(userId);

        if (!user.canSolveDailyQuiz()) {
            throw new BaseException(QuizResponseCode.TODAY_QUOTA_EXCEEDED);
        }

        Optional<DocumentSummary> latestSummaryOpt = documentSummaryRepository.findLatestByDocumentId(documentId);
        if (latestSummaryOpt.isPresent()) {
            DocumentSummary latestSummary = latestSummaryOpt.get();

            // 정상: 생성된 퀴즈가 아예 없는 경우 (생성 실패 또는 비동기 지연 등) 예외를 발생시키지 않음
            List<Quiz> generatedQuizzes = quizService.getQuizzesByDocumentSummaryId(latestSummary.getId());
            if (generatedQuizzes.isEmpty()) {
                return;
            }


            boolean isConsumed = userQuizService.getAllQuizzes(userId).stream()
                    .anyMatch(uq -> uq.getQuiz() != null &&
                            uq.getQuiz().getDocumentSummary() != null &&
                            uq.getQuiz().getDocumentSummary().getId().equals(latestSummary.getId()));

            // 에러: 생성된 퀴즈가 있지만 요약글 생성 요청 시
            if (!isConsumed) {
                throw new BaseException(QuizResponseCode.UNSOLVED_QUIZ_EXISTS);
            }
        }
    }
}
