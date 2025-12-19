package im.swyp.teumteumeat.domains.document.domain.service;

import im.swyp.teumteumeat.domains.document.persistence.entity.Document;
import im.swyp.teumteumeat.domains.llm.application.dto.response.LLMResponse;
import im.swyp.teumteumeat.domains.llm.domain.prompt.DocumentPrompt;
import im.swyp.teumteumeat.domains.llm.domain.prompt.QuizPrompt;
import im.swyp.teumteumeat.domains.llm.domain.service.LLMService;
import im.swyp.teumteumeat.domains.quiz.domain.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DocumentLLMService {

    private final LLMService llmService;
    private final QuizService quizService;

    @Transactional
    public void generateSummary(Document document) {
        String prompt = String.format(DocumentPrompt.GENERATE_PDF_SUMMARY.getTemplate(),
                document.getRawContent());
        String summary = llmService.generateContent(prompt);
        document.updateSummary(summary);
    }

    @Transactional
    public void createQuizzes(Document document) {
        String categoryName = "Available Document";
        String documentContent = document.getRawContent();
        int difficulty = 3; // 난이도 임시 고정

        String prompt = String.format(QuizPrompt.GENERATE_QUIZ.getTemplate(), categoryName, documentContent,
                difficulty);
        LLMResponse response = llmService.generateAnswer(prompt);

        response.quizzes().forEach(quizDto -> {
            quizService.createQuizFromPdfDocument(
                    document,
                    quizDto.question(),
                    quizDto.options() != null ? String.join(",", quizDto.options()) : "",
                    quizDto.answer(),
                    quizDto.type(),
                    quizDto.explanation());
        });
    }
}
