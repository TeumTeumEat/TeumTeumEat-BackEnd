package im.swyp.teumteumeat.domains.document.domain.service;

import im.swyp.teumteumeat.domains.document.persistence.entity.Document;
import im.swyp.teumteumeat.domains.document.persistence.entity.DocumentSummary;
import im.swyp.teumteumeat.domains.document.persistence.repository.DocumentSummaryRepository;
import im.swyp.teumteumeat.domains.document.persistence.repository.DocumentRepository;
import im.swyp.teumteumeat.domains.goal.persistence.entity.Goal;
import im.swyp.teumteumeat.domains.llm.domain.service.LLMService;
import im.swyp.teumteumeat.global.common.CommonResponseCode;
import im.swyp.teumteumeat.global.exception.BaseException;
import im.swyp.teumteumeat.global.util.ContentUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DocumentSummaryService {

    private final LLMService llmService;
    private final DocumentSummaryRepository documentSummaryRepository;
    private final DocumentRepository documentRepository;

    public Optional<DocumentSummary> getLatestSummaryByDocumentId(Long documentId) {
        return documentSummaryRepository.findLatestByDocumentId(documentId);
    }

    @Transactional
    public DocumentSummary generateTitleAndSaveSummary(Long documentId, String summaryContent) {
         // LazyInitializationException 방지 및 업데이트를 위한 영속성 상태 보장을 위해 Goal과 함께 문서 재조회
        Document fetchedDocument = documentRepository.findWithGoalById(documentId)
                .orElseThrow(() -> new BaseException(CommonResponseCode.NOT_FOUND));

        // 최근 30초 내에 동일한 문서에 대해 요약본이 생성되었는지 검사 (더블클릭/중복 생성 방지)
        LocalDateTime thirtySecondsAgo = LocalDateTime.now().minusSeconds(30);
        Optional<DocumentSummary> recentSummary = documentSummaryRepository
                .findFirstByDocumentIdAndCreatedDateAfterOrderByIdDesc(documentId, thirtySecondsAgo);

        if (recentSummary.isPresent()) {
            throw new BaseException(CommonResponseCode.BAD_REQUEST);
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
    }

    public boolean hasSummaryCreatedToday(Long userId) {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);
        return documentSummaryRepository.existsByDocument_User_IdAndCreatedDateBetween(userId, start, end);
    }
}
