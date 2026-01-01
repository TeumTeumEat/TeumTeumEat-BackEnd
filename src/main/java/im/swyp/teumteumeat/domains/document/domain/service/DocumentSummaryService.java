package im.swyp.teumteumeat.domains.document.domain.service;

import im.swyp.teumteumeat.domains.document.persistence.entity.Document;
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

@Service
@RequiredArgsConstructor
public class DocumentSummaryService {

    private final LLMService llmService;
    private final DocumentService documentService;
    private final ApplicationEventPublisher eventPublisher;

    public void generateSummary(Long documentId){
        eventPublisher.publishEvent(new DocumentSummaryEvent(documentId));
    }

    // TransactionalEventListener가 커밋을 확인한 후 비동기로 이 메서드를 호출
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void processSummary(DocumentSummaryEvent event) {
        Document document = documentService.getDocumentById(event.documentId());
        String prompt = String.format(DocumentPrompt.GENERATE_PDF_SUMMARY.getTemplate(),
                document.getRawContent());
        String summary = llmService.generateContent(prompt);
        document.updateSummary(summary);

        // 제목 생성
        String userGoal = document.getGoal().getPrompt();
        String topicInstruction = (userGoal != null && !userGoal.isEmpty()) ? userGoal : "전반적인 내용";

        String generatedTitle = llmService.generateTitle(summary, topicInstruction);
        document.updateTitle(generatedTitle);
    }
}
