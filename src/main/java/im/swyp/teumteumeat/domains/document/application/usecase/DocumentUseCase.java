package im.swyp.teumteumeat.domains.document.application.usecase;

import im.swyp.teumteumeat.domains.category.application.dto.response.DocumentErrorType;
import im.swyp.teumteumeat.domains.document.application.dto.request.DocumentCreateRequest;
import im.swyp.teumteumeat.domains.document.application.dto.request.OcrInitRequest;
import im.swyp.teumteumeat.domains.document.application.dto.request.OcrPartRequest;
import im.swyp.teumteumeat.domains.document.application.dto.response.DocumentListResponse;
import im.swyp.teumteumeat.domains.document.application.dto.response.DocumentResponse;
import im.swyp.teumteumeat.domains.document.application.mapper.DocumentMapper;
import im.swyp.teumteumeat.domains.document.application.mapper.DocumentPartMapper;
import im.swyp.teumteumeat.domains.document.domain.constant.FileStatus;
import im.swyp.teumteumeat.domains.document.domain.event.DocumentSseEvent;
import im.swyp.teumteumeat.domains.document.domain.service.DocumentService;
import im.swyp.teumteumeat.domains.document.persistence.entity.Document;
import im.swyp.teumteumeat.domains.document.persistence.entity.DocumentPart;
import im.swyp.teumteumeat.domains.goal.domain.service.GoalService;
import im.swyp.teumteumeat.domains.goal.persistence.entity.Goal;
import im.swyp.teumteumeat.domains.user.domain.service.UserService;
import im.swyp.teumteumeat.domains.user.persistence.entity.UserEntity;
import im.swyp.teumteumeat.global.annotation.UseCase;
import im.swyp.teumteumeat.global.sse.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.text.Normalizer;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@UseCase
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentUseCase {

    private final DocumentService documentService;
    private final UserService userService;
    private final GoalService goalService;
    private final NotificationService notificationService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Long uploadDocument(Long userId, Long goalId, DocumentCreateRequest request) {
        request = DocumentCreateRequest.builder()
                .fileKey(Normalizer.normalize(request.fileKey(), Normalizer.Form.NFC))
                .fileName(Normalizer.normalize(request.fileName(), Normalizer.Form.NFC))
                .build();

        UserEntity user = userService.getUserById(userId);
        Goal goal = goalService.getGoalById(goalId);

        // 임시 문서가 생성되어 있는 경우 User, Goal 업데이트
        Optional<Document> existDocument = documentService.getDocumnetByFileKeyOptional(request.fileKey());

        Document document;
        if (existDocument.isPresent()) {
            document = existDocument.get();
            document.updateUser(user);
            document.updateGoal(goal);
        }
        // 아직 생성이 안된 경우 문서 생성
        else {
            document = DocumentMapper.toDocument(user, goal, request);
            documentService.createDocument(document);
        }

        return document.getId();
    }

    // fileKey로 문서 parts 설정
    @Transactional
    public void setParts(OcrInitRequest request) {
        // 이미 문서 Entity가 생성되어 있으면 가져오고, 없으면 임시 문서 생성
        Document document = documentService.getOrSaveDocument(request.fileKey(), request.fileName());

        // OCR 처리가 필요한 경우
        if (request.needOcr()) {
            document.updateTotalParts(request.totalParts());
            document.updateEstimateTime(request.estimateTime());
            document.updateStatus(FileStatus.PROCESSING);
        }
        // PDF에서 텍스트 추출이 완료된 경우
        else {
            document.updateRawContent(request.rawContent());
            document.deleteEstimateTime();

            // Summary (요약) (제거: Deadlock 방지 및 Lazy Generation 유도)
            // documentSummaryService.generateSummaryAsync(document.getId());
            document.updateStatus(FileStatus.COMPLETED);
        }
        // PROCESSING 또는 COMPLETED 알림을 전송
        eventPublisher.publishEvent(new DocumentSseEvent(document));
    }

    @Transactional
    public void saveParts(OcrPartRequest request) {
        Document document = documentService.getDocumentByFileKey(request.fileKey());

        DocumentPart documentPart = DocumentPartMapper.toDocumentPart(
                document,
                request.partIndex(),
                request.ocrText());
        documentService.createDocumentPart(documentPart);

        if (document.isAllPartsCollected()) {
            List<DocumentPart> parts = document.getParts();

            String combinedText = parts.stream()
                    .sorted(Comparator.comparing(DocumentPart::getPartIndex))
                    .map(DocumentPart::getOcrText)
                    .collect(Collectors.joining(" "));
            document.updateRawContent(combinedText);

            // Summary (요약) - 비동기 (제거: Deadlock 방지 및 Lazy Generation 유도)
            // documentSummaryService.generateSummaryAsync(document.getId());
            document.updateStatus(FileStatus.COMPLETED);
            document.getParts().clear();
            // 완료 알림을 전송
            eventPublisher.publishEvent(new DocumentSseEvent(document));
        }
    }

    // 해당 목표의 모든 문서 반환
    public DocumentListResponse getDocuments(Long userId, Long goalId) {
        Goal goal = goalService.getGoalById(goalId);
        goal.validateOwner(userId);

        List<Document> documents = documentService.getDocumentsByGoalId(goalId);
        List<DocumentResponse> responses = documents.stream().map(DocumentMapper::fromDocument).toList();
        return DocumentMapper.toDocumentListResponse(responses);
    }

    // 특정 문서 반환 (단순 조회)
    public DocumentResponse getDocument(Long userId, Long goalId, Long documentId) {
        Goal goal = goalService.getGoalById(goalId);
        goal.validateOwner(userId);

        Document document = documentService.getDocumentById(documentId);
        document.validateOwner(userId);
        document.validateBelongTo(goalId);

        return DocumentMapper.fromDocument(document);
    }

    // 해당 목표의 모든 문서 삭제
    @Transactional
    public void deleteDocuments(Long userId, Long goalId) {
        Goal goal = goalService.getGoalById(goalId);
        goal.validateOwner(userId);

        documentService.deleteDocumentsByGoalId(goalId);
    }

    // 특정 문서 삭제
    @Transactional
    public void deleteDocument(Long userId, Long goalId, Long documentId) {
        Goal goal = goalService.getGoalById(goalId);
        goal.validateOwner(userId);

        Document document = documentService.getDocumentById(documentId);
        document.validateOwner(userId);
        document.validateBelongTo(goalId);

        documentService.deleteDocument(documentId);
    }

    public SseEmitter subscribe(Long userId, Long goalId, Long documentId, @Nullable String lastEventId) {
        // 목표 인가
        Goal goal = goalService.getGoalById(goalId);
        goal.validateOwner(userId);

        // 문서 인가
        Document document = documentService.getDocumentById(documentId);
        document.validateOwner(userId);
        document.validateBelongTo(goalId);

        // SSE 구독
        return notificationService.subscribe(
                lastEventId,
                // 신규 구독이거나 재전송이 되지 않았을 때만 최신 상태를 반환
                () -> eventPublisher.publishEvent(new DocumentSseEvent(document)),
                userId,
                documentId);
    }

    @Transactional
    public void handleOcrFailure(String fileKey, DocumentErrorType errorType) {
        documentService.getDocumnetByFileKeyOptional(fileKey).ifPresent(document -> {
            document.updateStatusToFailed(errorType);
            eventPublisher.publishEvent(new DocumentSseEvent(document));
        });
    }
}
