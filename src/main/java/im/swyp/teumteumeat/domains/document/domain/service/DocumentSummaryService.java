package im.swyp.teumteumeat.domains.document.domain.service;

import im.swyp.teumteumeat.domains.document.persistence.entity.Document;
import im.swyp.teumteumeat.domains.goal.persistence.entity.Goal;
import im.swyp.teumteumeat.domains.llm.domain.prompt.DocumentPrompt;
import im.swyp.teumteumeat.domains.llm.domain.service.LLMService;
import lombok.RequiredArgsConstructor;
import im.swyp.teumteumeat.domains.document.persistence.entity.DocumentSummary;
import im.swyp.teumteumeat.domains.document.persistence.repository.DocumentSummaryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DocumentSummaryService {

    private final LLMService llmService;
    private final DocumentService documentService;
    private final DocumentSummaryRepository documentSummaryRepository;

    @Transactional
    public DocumentSummary generateSummary(Document document) {
        String prompt = String.format(DocumentPrompt.GENERATE_PDF_SUMMARY.getTemplate(),
                document.getRawContent());
        String summaryContent = llmService.generateContent(prompt);
        // data truncation
        summaryContent = summaryContent.substring(0, Math.min(summaryContent.length(), 500));

        // 제목 생성
        String topicInstruction = Optional.ofNullable(document.getGoal())
                .map(Goal::getPrompt)
                .filter(p -> !p.isEmpty())
                .orElse("전반적인 내용");

        String generatedTitle = llmService.generateTitle(summaryContent, topicInstruction);

        DocumentSummary documentSummary = DocumentSummary.builder()
                .document(document)
                .summary(summaryContent)
                .title(generatedTitle)
                .build();

        return documentSummaryRepository.save(documentSummary);
    }
}
