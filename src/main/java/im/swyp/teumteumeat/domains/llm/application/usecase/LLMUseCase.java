package im.swyp.teumteumeat.domains.llm.application.usecase;

import im.swyp.teumteumeat.domains.llm.application.dto.request.LLMRequest;
import im.swyp.teumteumeat.domains.llm.application.dto.response.LLMResponse;
import im.swyp.teumteumeat.domains.llm.domain.service.LLMService;
import im.swyp.teumteumeat.global.annotation.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LLMUseCase {

    private final LLMService llmService;

    public LLMResponse generateQuiz(LLMRequest request) {
        return llmService.generateAnswer(request);
    }
}
