package im.swyp.teumteumeat.domains.llm.domain.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import im.swyp.teumteumeat.domains.llm.application.dto.response.LLMResponse;
import im.swyp.teumteumeat.domains.llm.domain.constant.LLMResponseCode;
import im.swyp.teumteumeat.domains.llm.domain.prompt.DocumentPrompt;
import im.swyp.teumteumeat.global.exception.BaseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class LLMService {

    @Value("${spring.ai.provider:openai}")
    private String activeProvider;

    // OpenAI Config
    @Value("${spring.ai.openai.api-key}")
    private String openAiApiKey;
    @Value("${spring.ai.openai.chat.options.model}")
    private String openAiModel;
    @Value("${spring.ai.openai.base-url:https://api.openai.com/v1}")
    private String openAiBaseUrl;

    // Naver Config
    @Value("${spring.ai.naver.api-key:}")
    private String naverApiKey;
    @Value("${spring.ai.naver.chat.options.model:}")
    private String naverModel;
    @Value("${spring.ai.naver.base-url:}")
    private String naverBaseUrl;

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

    private String callOpenAiApi(String promptMessage, String systemRole, boolean jsonMode) {
        String currentApiKey;
        String currentModel;
        String currentBaseUrl;

        if ("naver".equalsIgnoreCase(activeProvider)) {
            currentApiKey = naverApiKey;
            currentModel = naverModel;
            currentBaseUrl = naverBaseUrl;
            log.debug("Using Naver LLM Provider");
        } else {
            currentApiKey = openAiApiKey;
            currentModel = openAiModel;
            currentBaseUrl = openAiBaseUrl;
        }

        // OpenAI API 호출 (RestClient 사용)
        RestClient restClient = RestClient.builder()
                .baseUrl(currentBaseUrl)
                .defaultHeader("Authorization", "Bearer " + currentApiKey)
                .build();

        try {
            // 요청 바디 구성
            Map<String, Object> requestBody = new java.util.HashMap<>();
            requestBody.put("model", currentModel);
            requestBody.put("messages", List.of(
                    Map.of("role", "system", "content", systemRole),
                    Map.of("role", "user", "content", promptMessage)));

            if (jsonMode) {
                requestBody.put("response_format", Map.of("type", "json_object"));
            }

            // 요청 전송
            OpenAiResponse response = restClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(OpenAiResponse.class);

            // 응답 추출
            if (response == null || response.choices() == null || response.choices().isEmpty()) {
                throw new RuntimeException("OpenAI 응답이 비어있습니다.");
            }

            String content = response.choices().get(0).message().content();
            log.debug("AI Raw 응답: {}", content);

            return content;

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