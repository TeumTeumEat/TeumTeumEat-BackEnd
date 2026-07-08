package im.swyp.teumteumeat.domains.llm.domain.service;

import im.swyp.teumteumeat.domains.llm.application.dto.response.LLMResponse;
import im.swyp.teumteumeat.domains.llm.domain.constant.LLMResponseCode;
import im.swyp.teumteumeat.domains.llm.domain.prompt.DocumentPrompt;
import im.swyp.teumteumeat.global.exception.BaseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.AdvisorParams;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.StructuredOutputValidationAdvisor;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.ai.retry.TransientAiException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.function.Supplier;

@Service
@Slf4j
public class LLMService {

    // Spring AI 1.1의 에러 메시지 포맷은 "HTTP {status} - {body}"
    private static final String STATUS_TOO_MANY_REQUESTS = "HTTP 429";

    private final ChatClient chatClient;

    public LLMService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    // 스키마 검증 실패 시 에러 내용을 프롬프트에 붙여 재시도한다 (기본 3회)
    private final StructuredOutputValidationAdvisor quizSchemaValidationAdvisor =
            StructuredOutputValidationAdvisor.builder()
                    .outputType(LLMResponse.class)
                    .build();

    public LLMResponse generateAnswer(String promptMessage) {
        // 스키마를 프롬프트 텍스트가 아닌 OpenAI Structured Outputs(json_schema, strict)로 전달한다
        return executeWithExceptionHandling(() -> chatClient.prompt()
                .advisors(AdvisorParams.ENABLE_NATIVE_STRUCTURED_OUTPUT)
                .advisors(quizSchemaValidationAdvisor)
                .system("당신은 퀴즈 생성 전문가입니다.")
                .user(promptMessage)
                .call()
                .entity(LLMResponse.class));
    }

    public String generateContent(String promptMessage) {
        return executeWithExceptionHandling(() -> chatClient.prompt()
                .system("당신은 교육 자료 생성 전문가입니다.")
                .user(promptMessage)
                .call()
                .content());
    }

    public String generateTitle(String content, String userGoal) {
        String prompt = String.format(DocumentPrompt.GENERATE_TITLE.getTemplate(), userGoal, content);
        return executeWithExceptionHandling(() -> chatClient.prompt()
                .system("당신은 요약 전문가입니다.")
                .user(prompt)
                .call()
                .content());
    }

    // 스트림 내부 에러는 Flux의 error 채널로 전달되어 구독부에서 처리한다
    public Flux<String> generateContentStream(String promptMessage) {
        return chatClient.prompt()
                .system("당신은 교육 자료 생성 전문가입니다.")
                .user(promptMessage)
                .stream()
                .content();
    }

    private <T> T executeWithExceptionHandling(Supplier<T> apiCall) {
        try {
            return apiCall.get();
        } catch (NonTransientAiException e) {
            // 4xx 에러 (재시도 대상 아님)
            log.error("AI 요청 클라이언트 에러: {}", e.getMessage(), e);
            if (e.getMessage() != null && e.getMessage().startsWith(STATUS_TOO_MANY_REQUESTS)) {
                throw new BaseException(LLMResponseCode.AI_QUOTA_EXCEEDED);
            }
            throw new BaseException(LLMResponseCode.AI_INVALID_REQUEST);
        } catch (TransientAiException e) {
            // 5xx 에러. Spring AI 기본 RetryTemplate의 재시도가 모두 소진된 경우
            log.error("AI 요청 서버 에러: {}", e.getMessage(), e);
            throw new BaseException(LLMResponseCode.AI_SERVER_ERROR);
        } catch (Exception e) {
            log.error("AI 요청 중 알 수 없는 에러 발생", e);
            throw new BaseException(LLMResponseCode.AI_GENERATION_FAILED);
        }
    }
}
