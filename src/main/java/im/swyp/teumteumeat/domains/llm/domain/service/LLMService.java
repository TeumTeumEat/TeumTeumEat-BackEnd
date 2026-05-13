package im.swyp.teumteumeat.domains.llm.domain.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import im.swyp.teumteumeat.domains.llm.application.dto.response.LLMResponse;
import im.swyp.teumteumeat.domains.llm.domain.constant.LLMResponseCode;
import im.swyp.teumteumeat.domains.llm.domain.prompt.DocumentPrompt;
import im.swyp.teumteumeat.global.exception.BaseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Service
@Slf4j
public class LLMService {

    @Value("${spring.ai.openai.chat.options.model}")
    private String openAiModel;

    private final RestClient restClient;

    private final WebClient webClient;

    private final ObjectMapper objectMapper;

    public LLMService(@Qualifier("openAiRestClient") RestClient restClient,
                      @Value("${spring.ai.openai.chat.options.model}") String openAiModel, @Qualifier("openAiWebClient") WebClient webClient, ObjectMapper objectMapper) {
        this.restClient = restClient;
        this.openAiModel = openAiModel;
        this.webClient = webClient;
        this.objectMapper = objectMapper;
    }

    public LLMResponse generateAnswer(String promptMessage) {
        // Prompt에 포맷 넣기
        BeanOutputConverter<LLMResponse> converter = new BeanOutputConverter<>(LLMResponse.class);

        // 공통 API 호출 (JSON 모드 활성화)
        String content = callOpenAiApi(promptMessage, "당신은 퀴즈 생성 전문가입니다.", true);

        // 파싱 (DTO로 변환)
        return converter.convert(content);
    }

    public String generateContent(String promptMessage) {
        // 공통 API 호출 (JSON 모드 비활성화)
        return callOpenAiApi(promptMessage, "당신은 교육 자료 생성 전문가입니다.", false);
    }

    public String generateTitle(String content, String userGoal) {
        String prompt = String.format(
                DocumentPrompt.GENERATE_TITLE.getTemplate(), userGoal,
                content);
        return callOpenAiApi(prompt, "당신은 요약 전문가입니다.", false);
    }

    public Flux<String> generateContentStream(String promptMessage) {
        return callOpenAiApiStream(promptMessage, "당신은 교육 자료 생성 전문가입니다.", false);
    }

    private String callOpenAiApi(String promptMessage, String systemRole, boolean jsonMode) {
        return executeWithExceptionHandling(() -> {
            Map<String, Object> requestBody = createRequestBody(promptMessage, systemRole, jsonMode, false);

            OpenAiResponse response = restClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(OpenAiResponse.class);

            if (response == null || response.choices() == null || response.choices().isEmpty()) {
                throw new RuntimeException("OpenAI 응답이 비어있습니다.");
            }

            String content = response.choices().get(0).message().content();
            log.debug("AI Raw 응답: {}", content);

            return content;
        });
    }

    private Flux<String> callOpenAiApiStream(String promptMessage, String systemRole, boolean jsonMode) {
        return executeWithExceptionHandling(() -> {
            Map<String, Object> requestBody = createRequestBody(promptMessage, systemRole, jsonMode, true);

            return webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(requestBody) // 프롬프트 DTO
                    .retrieve()
                    .bodyToFlux(String.class) // 응답을 스트림(Flux)으로 받음
                    // "[DONE]" 등 불필요한 신호 필터링
                    .filter(chunk -> chunk != null && !chunk.contains("[DONE]"))
                    // 복잡한 JSON에서 실제 글자(content)만 추출
                    .map(this::extractContentFromChunk)
                    // 빈 문자열 무시
                    .filter(text -> text != null && !text.isEmpty());
        });
    }

    private Map<String, Object> createRequestBody(String promptMessage, String systemRole, boolean jsonMode, boolean stream) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", openAiModel);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", systemRole),
                Map.of("role", "user", "content", promptMessage)));

        if (jsonMode) {
            requestBody.put("response_format", Map.of("type", "json_object"));
        }

        if (stream) {
            requestBody.put("stream", true);
        }

        return requestBody;
    }

    private <T> T executeWithExceptionHandling(Supplier<T> apiCall) {
        try {
            return apiCall.get();
        } catch (HttpClientErrorException e) {
            log.error("AI 요청 클라이언트 에러: Status={}, Body={}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            if (e.getStatusCode().value() == 429) {
                throw new BaseException(LLMResponseCode.AI_QUOTA_EXCEEDED);
            } else if (e.getStatusCode().value() >= 400 && e.getStatusCode().value() < 500) {
                throw new BaseException(LLMResponseCode.AI_INVALID_REQUEST);
            }
            throw new BaseException(LLMResponseCode.AI_GENERATION_FAILED);

        } catch (HttpServerErrorException e) {
            log.error("AI 요청 서버 에러: Status={}, Body={}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new BaseException(LLMResponseCode.AI_SERVER_ERROR);

        } catch (Exception e) {
            log.error("AI 요청 중 알 수 없는 에러 발생", e);
            throw new BaseException(LLMResponseCode.AI_GENERATION_FAILED);
        }
    }

    // OpenAI의 청크 JSON 구조에서 텍스트만 빼내기
    private String extractContentFromChunk(String jsonChunk) {
        try {
            JsonNode rootNode = objectMapper.readTree(jsonChunk);
            JsonNode choices = rootNode.path("choices");
            if (choices.isArray() && !choices.isEmpty()) {
                JsonNode delta = choices.get(0).path("delta");
                if (delta.has("content")) {
                    return delta.get("content").asText();
                }
            }
        } catch (Exception e) {
            log.trace("Failed to parse chunk (might be empty delta): {}", jsonChunk);
        }
        return "";
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record OpenAiResponse(List<Choice> choices) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Choice(Message message) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Message(String content) {
    }
}