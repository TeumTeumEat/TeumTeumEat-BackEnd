package im.swyp.teumteumeat.domains.document.domain.service;

import im.swyp.teumteumeat.domains.document.persistence.entity.Document;
import im.swyp.teumteumeat.domains.document.persistence.entity.DocumentSummary;
import im.swyp.teumteumeat.domains.document.persistence.repository.DocumentSummaryRepository;
import im.swyp.teumteumeat.domains.goal.persistence.entity.Goal;
import im.swyp.teumteumeat.domains.llm.domain.prompt.DocumentPrompt;
import im.swyp.teumteumeat.domains.llm.domain.service.LLMService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DocumentSummaryService {

    private final LLMService llmService;
    private final DocumentService documentService;
    private final ApplicationEventPublisher eventPublisher;
    private final DocumentSummaryRepository documentSummaryRepository;

    public void generateSummary(Long documentId) {
        eventPublisher.publishEvent(new DocumentSummaryEvent(documentId));
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void processSummary(DocumentSummaryEvent event) {
        Document document = documentService.getDocumentById(event.documentId());
        String prompt = String.format(DocumentPrompt.GENERATE_PDF_SUMMARY.getTemplate(),
                document.getRawContent());
        String summaryContent = llmService.generateContent(prompt);
        summaryContent = summaryContent.substring(0, Math.min(summaryContent.length(), 500));

        // 제목 생성
        String topicInstruction = Optional.ofNullable(document.getGoal())
                .map(Goal::getPrompt)
                .filter(p -> !p.isEmpty())
                .orElse("전반적인 내용");

        String generatedTitle = llmService.generateTitle(summaryContent, topicInstruction);
        document.updateTitle(generatedTitle);

        // DocumentSummary 저장 (변경된 로직: 별도 엔티티로 저장)
        DocumentSummary documentSummary = DocumentSummary.builder()
                .document(document)
                .summary(summaryContent)
                .title(generatedTitle)
                .build();
        documentSummaryRepository.save(documentSummary);
    }
}
