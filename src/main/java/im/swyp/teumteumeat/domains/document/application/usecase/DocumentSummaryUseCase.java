package im.swyp.teumteumeat.domains.document.application.usecase;

import im.swyp.teumteumeat.domains.document.application.dto.event.DocumentSummaryGenerationEvent;
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
import im.swyp.teumteumeat.domains.quiz.application.usecase.QuizUseCase;
import im.swyp.teumteumeat.domains.quiz.domain.constant.QuizResponseCode;
import im.swyp.teumteumeat.domains.userQuiz.domain.service.UserQuizService;
import im.swyp.teumteumeat.global.annotation.UseCase;
import im.swyp.teumteumeat.global.common.CommonResponseCode;
import im.swyp.teumteumeat.global.exception.BaseException;
import im.swyp.teumteumeat.domains.user.domain.constant.Role;
import im.swyp.teumteumeat.domains.user.domain.service.UserService;
import im.swyp.teumteumeat.domains.user.persistence.entity.UserEntity;
import im.swyp.teumteumeat.global.sse.component.SseProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

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
    private final ApplicationEventPublisher eventPublisher;
    private final SseProvider sseProvider;

    @Transactional
    public DocumentDetailResponse getSummaryForView(Long userId, Long goalId, Long documentId) {
        Goal goal = goalService.getGoalById(goalId);
        goal.validateOwner(userId);

        if (goal.getEndDate().isBefore(LocalDate.now())) {
            throw new BaseException(GoalResponseCode.GOAL_EXPIRED);
        }
        if (goal.isCompleted()) {
            throw new BaseException(GoalResponseCode.GOAL_COMPLETED);
        }

        boolean isFirstTime = !userQuizService.hasSolvedAnyQuizEver(userId);
        Document document = documentService.getDocumentById(documentId);
        document.validateOwner(userId);

        if (document.getRawContent() == null || document.getRawContent().trim().isEmpty()) {
            throw new BaseException(DocumentResponseCode.DOCUMENT_NOT_READY);
        }

        UserEntity user = userService.getUserById(userId);
        boolean outOfQuota = !user.canSolveDailyQuiz();
        boolean hasSolvedToday = user.getRole() != Role.ADMIN && outOfQuota;

        // 단순 조회 로직: 가장 최신의 요약글 찾아서 반환 (자동 생성 x)
        Optional<DocumentSummary> latestSummaryOpt = documentSummaryRepository.findLatestByDocumentId(documentId);
        DocumentSummary summary = latestSummaryOpt.orElseThrow(() -> new BaseException(CommonResponseCode.NOT_FOUND));

        return DocumentMapper.toDocumentDetailResponse(document, summary, hasSolvedToday, isFirstTime);
    }

    //  pdf 요약글 생성 (학습 시 사용 되는 메서드)
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void createSummaryAsync(Long userId, Long goalId, Long documentId) {
        Goal goal = goalService.getGoalById(goalId);
        goal.validateOwner(userId);

        // 1. Goal 만료 및 달성 확인
        if (goal.getEndDate().isBefore(LocalDate.now())) {
            throw new BaseException(GoalResponseCode.GOAL_EXPIRED);
        }
        if (goal.isCompleted()) {
            throw new BaseException(GoalResponseCode.GOAL_COMPLETED);
        }

        // 2. 쿼타 확인 - ADMIN 제외
        UserEntity user = userService.getUserById(userId);
        if (user.getRole() != Role.ADMIN && !user.canSolveDailyQuiz()) {
            throw new BaseException(QuizResponseCode.TODAY_QUOTA_EXCEEDED);
        }

        Document document = documentService.getDocumentById(documentId);
        document.validateOwner(userId);

        // 3. 아직 해당 문서에 대해 풀지 않은(미해결) 최신 요약글이 있는지 확인
        if (user.getRole() != Role.ADMIN) {
            Optional<DocumentSummary> latestSummaryOpt = documentSummaryRepository.findLatestByDocumentId(documentId);
            if (latestSummaryOpt.isPresent()) {
                DocumentSummary latestSummary = latestSummaryOpt.get();
                boolean isConsumed = userQuizService.getAllQuizzes(userId).stream()
                        .anyMatch(uq -> uq.getQuiz() != null &&
                                uq.getQuiz().getDocumentSummary() != null &&
                                uq.getQuiz().getDocumentSummary().getId().equals(latestSummary.getId()));

                if (!isConsumed) {
                    throw new BaseException(QuizResponseCode.UNSOLVED_QUIZ_EXISTS);
                }
            }
        }

        boolean isFirstTime = !userQuizService.hasSolvedAnyQuizEver(userId);
        // 요약글 생성 이벤트 publish
        eventPublisher.publishEvent(new DocumentSummaryGenerationEvent(userId, goal, document, isFirstTime));
    }

    @Async
    @EventListener
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void handleDocumentSummaryGenerationEvent(DocumentSummaryGenerationEvent event) {
        // 요약글 생성 후 퀴즈 생성
        try {
            DocumentSummary summary = documentSummaryService.generateSummary(event.document());
            quizUseCase.createQuizzesForPdfDocument(event.document(), summary);

            DocumentDetailResponse response = DocumentMapper.toDocumentDetailResponse(event.document(), summary, false, event.isFirstTime());
            sseProvider.sendEvent(event.userId().toString(), "DOCUMENT_GENERATED", response);
        } catch (Exception e) {
            log.error("PDF 요약글 생성 실패 userId: {}", event.userId(), e);
            sseProvider.sendEvent(event.userId().toString(), "GENERATION_ERROR", "PDF 요약글 생성에 실패했습니다.");
        }
    }
}
