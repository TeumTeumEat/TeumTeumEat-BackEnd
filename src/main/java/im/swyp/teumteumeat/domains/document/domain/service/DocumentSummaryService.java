package im.swyp.teumteumeat.domains.document.domain.service;

import im.swyp.teumteumeat.domains.document.persistence.entity.Document;
import im.swyp.teumteumeat.domains.llm.domain.prompt.DocumentPrompt;
import im.swyp.teumteumeat.domains.llm.domain.service.LLMService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DocumentSummaryService {

    private final LLMService llmService;
    private final DocumentService documentService;

    @Async
    @Transactional
    public void generateSummary(Long documentId) {
        Document document = documentService.getDocumentById(documentId);
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
