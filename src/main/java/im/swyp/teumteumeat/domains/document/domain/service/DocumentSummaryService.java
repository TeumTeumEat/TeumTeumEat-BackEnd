package im.swyp.teumteumeat.domains.document.domain.service;

import im.swyp.teumteumeat.domains.document.persistence.entity.Document;
import im.swyp.teumteumeat.domains.document.persistence.entity.DocumentSummary;
import im.swyp.teumteumeat.domains.document.persistence.repository.DocumentSummaryRepository;
import im.swyp.teumteumeat.domains.goal.persistence.entity.Goal;
import im.swyp.teumteumeat.domains.llm.domain.prompt.DocumentPrompt;
import im.swyp.teumteumeat.domains.llm.domain.service.LLMService;
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

    @Transactional
    public DocumentSummary generateSummary(Document document) {
        String prompt = String.format(DocumentPrompt.GENERATE_PDF_SUMMARY.getTemplate(),
                document.getRawContent());
        String summaryContent = llmService.generateContent(prompt);
        // LLM이 길게 생성할 경우를 대비하여 길이 제한 (공백 포함 600자) - 문장 단위로 자르기
        summaryContent = truncateContentSafe(summaryContent);

        // 제목 생성
        String topicInstruction = Optional.ofNullable(document.getGoal())
                .map(Goal::getPrompt)
                .filter(p -> !p.isEmpty())
                .orElse("전반적인 내용");

        String generatedTitle = llmService.generateTitle(summaryContent, topicInstruction);
        document.updateTitle(generatedTitle);

        // DocumentSummary 저장
        DocumentSummary documentSummary = DocumentSummary.builder()
                .document(document)
                .summary(summaryContent)
                .title(generatedTitle)
                .build();
        return documentSummaryRepository.save(documentSummary);
    }

    public boolean hasSummaryCreatedToday(Long userId) {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);
        return documentSummaryRepository.existsByDocument_User_IdAndCreatedDateBetween(userId, start, end);
    }

    private String truncateContentSafe(String content) {
        if (content == null || content.length() <= 600) {
            return content;
        }
        String truncated = content.substring(0, 600);
        int lastPeriodIndex = truncated.lastIndexOf(".");
        if (lastPeriodIndex != -1) {
            return truncated.substring(0, lastPeriodIndex + 1);
        }
        return truncated;
    }
}
