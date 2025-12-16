package im.swyp.teumteumeat.domains.llm.application.usecase;

import im.swyp.teumteumeat.domains.llm.application.dto.request.LLMRequest;
import im.swyp.teumteumeat.domains.llm.application.dto.response.LLMResponse;
import im.swyp.teumteumeat.domains.llm.domain.prompt.DocumentPrompt;
import im.swyp.teumteumeat.domains.llm.domain.prompt.QuizPrompt;
import im.swyp.teumteumeat.domains.llm.domain.service.LLMService;
import org.springframework.ai.converter.BeanOutputConverter;
import im.swyp.teumteumeat.global.annotation.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LLMUseCase {

    private final LLMService llmService;

    public LLMResponse generateQuiz(LLMRequest request) {
        // Prompt에 포맷 넣기
        BeanOutputConverter<LLMResponse> converter = new BeanOutputConverter<>(LLMResponse.class);

        // 프롬프트 메시지 구성
        String promptMessage = String.format(QuizPrompt.GENERATE_QUIZ.getTemplate(),
                request.category(),
                request.level())
                + "\n반드시 다음 JSON 형식을 지켜주세요:\n" + converter.getFormat();

        return llmService.generateAnswer(promptMessage);
    }

    public String generateDocumentContent(String category) {
        String promptMessage = String.format(DocumentPrompt.GENERATE_DOCUMENT.getTemplate(), category);
        return llmService.generateContent(promptMessage);
    }
}
