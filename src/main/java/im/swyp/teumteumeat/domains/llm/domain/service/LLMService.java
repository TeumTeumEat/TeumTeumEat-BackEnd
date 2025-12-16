package im.swyp.teumteumeat.domains.llm.domain.service;

import im.swyp.teumteumeat.domains.llm.application.dto.request.LLMRequest;
import im.swyp.teumteumeat.domains.llm.application.dto.response.LLMResponse;
import im.swyp.teumteumeat.domains.llm.domain.prompt.QuizPrompt;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class LLMService {
    private final ChatClient chatClient;

    public LLMResponse generateAnswer(LLMRequest llmRequest) {
        BeanOutputConverter<LLMResponse> converter = new BeanOutputConverter<>(LLMResponse.class);

        PromptTemplate promptTemplate = new PromptTemplate(QuizPrompt.GENERATE_QUIZ.getTemplate() + "\n{format}");
        Prompt prompt = promptTemplate.create(Map.of(
                "category", llmRequest.getCategory(),
                "type", llmRequest.getType(),
                "level", llmRequest.getLevel(),
                "format", converter.getFormat()));

        // AI 호출 및 결과 반환
        String content = chatClient.prompt(prompt)
                .call()
                .content();

        return converter.convert(content);
    }
}