package im.swyp.teumteumeat.domains.document.domain.service;

import im.swyp.teumteumeat.domains.document.persistence.entity.Document;
import im.swyp.teumteumeat.domains.document.persistence.entity.DocumentSummary;
import im.swyp.teumteumeat.domains.document.persistence.repository.DocumentSummaryRepository;
import im.swyp.teumteumeat.domains.document.persistence.repository.DocumentRepository;
import im.swyp.teumteumeat.domains.goal.persistence.entity.Goal;
import im.swyp.teumteumeat.domains.llm.domain.service.LLMService;
import im.swyp.teumteumeat.domains.user.domain.constant.Role;
import im.swyp.teumteumeat.global.common.CommonResponseCode;
import im.swyp.teumteumeat.global.component.DistributedLockFacade;
import im.swyp.teumteumeat.global.exception.BaseException;
import im.swyp.teumteumeat.global.util.ContentUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class DocumentSummaryService {

    private final LLMService llmService;
    private final DocumentSummaryRepository documentSummaryRepository;
    private final DocumentRepository documentRepository;
    private final DistributedLockFacade distributedLockFacade;

    public Optional<DocumentSummary> getExistingSummaryToday(Long documentId, boolean isAdmin) {
        if (isAdmin) return Optional.empty();

        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);
        return documentSummaryRepository.findByDocumentIdAndCreatedDateBetween(documentId, start, end);
    }

    public Optional<DocumentSummary> getLatestSummaryByDocumentId(Long documentId) {
        return documentSummaryRepository.findLatestByDocumentId(documentId);
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public DocumentSummary generateTitleAndSaveSummary(Long documentId, String summaryContent) {
        String lockKey = "lock:document_summary:generation:" + documentId;

        return distributedLockFacade.tryExecuteWithLock(lockKey, 30, 60, TimeUnit.SECONDS, () -> {
            // LazyInitializationException 방지 및 업데이트를 위한 영속성 상태 보장을 위해 Goal과 함께 문서 재조회
            Document fetchedDocument = documentRepository.findWithGoalById(documentId)
                    .orElseThrow(() -> new BaseException(CommonResponseCode.NOT_FOUND));

            // 락 내부에서 이중 체크 (Double-Check)
            LocalDateTime start = LocalDate.now().atStartOfDay();
            LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);

            boolean isAdmin = fetchedDocument.getUser()
                    .getRole() == Role.ADMIN;

            if (!isAdmin) {
                Optional<DocumentSummary> existingSummary = documentSummaryRepository
                        .findByDocumentIdAndCreatedDateBetween(documentId, start, end);
                if (existingSummary.isPresent()) {
                    return existingSummary.get(); // 이미 생성되었다면 기존 반환
                }
            }

            // LLM이 길게 생성할 경우를 대비하여 길이 제한 (공백 포함 600자) - 문장 단위로 자르기
            String truncatedContent = ContentUtils.truncateContentSafe(summaryContent);

            // 제목 생성
            String topicInstruction = Optional.ofNullable(fetchedDocument.getGoal())
                    .map(Goal::getPrompt)
                    .filter(p -> !p.isEmpty())
                    .orElse("전반적인 내용");

            String generatedTitle = llmService.generateTitle(truncatedContent, topicInstruction);
            fetchedDocument.updateTitle(generatedTitle);
            documentRepository.save(fetchedDocument); // 분리된(detached) 혹은 재조회된 엔티티에 대한 명시적 저장

            // DocumentSummary 저장
            DocumentSummary documentSummary = DocumentSummary.builder()
                    .document(fetchedDocument)
                    .summary(truncatedContent)
                    .title(generatedTitle)
                    .build();
            return documentSummaryRepository.save(documentSummary);
        }).orElseGet(() -> {
            // 락 획득 실패 (Timeout 30s) -> 다른 스레드가 생성했는지 확인
            LocalDateTime start = LocalDate.now().atStartOfDay();
            LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);
            return documentSummaryRepository.findByDocumentIdAndCreatedDateBetween(documentId, start, end)
                    .orElseThrow(() -> new BaseException(CommonResponseCode.INTERNAL_SERVER_ERROR));
        });
    }

    public boolean hasSummaryCreatedToday(Long userId) {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);
        return documentSummaryRepository.existsByDocument_User_IdAndCreatedDateBetween(userId, start, end);
    }
}
