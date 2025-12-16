package im.swyp.teumteumeat.domains.llm.domain.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import im.swyp.teumteumeat.domains.llm.application.dto.response.LLMResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class LLMService {

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    public LLMResponse generateAnswer(String promptMessage) {
        // Prompt에 포맷 넣기
        BeanOutputConverter<LLMResponse> converter = new BeanOutputConverter<>(LLMResponse.class);

        // OpenAI API 호출 (RestClient 사용)
        RestClient restClient = RestClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();

        try {
            // 요청 전송
            OpenAiResponse response = restClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "model", "gpt-4o-mini", // 모델명 확인
                            "messages", List.of(
                                    Map.of("role", "system", "content", "당신은 퀴즈 생성 전문가입니다."),
                                    Map.of("role", "user", "content", promptMessage)),
                            "response_format", Map.of("type", "json_object") // JSON 모드 활성화
                    ))
                    .retrieve()
                    .body(OpenAiResponse.class); // 아래 정의한 DTO로 받음

            // 응답 추출 및 DTO 변환
            if (response == null || response.choices() == null || response.choices().isEmpty()) {
                throw new RuntimeException("OpenAI 응답이 비어있습니다.");
            }

            String content = response.choices().get(0).message().content();
            log.info("AI Raw 응답: {}", content);

            // 파싱 (DTO로 변환)
            return converter.convert(content);

        } catch (Exception e) {
            log.error("AI 요청 중 에러 발생", e);
            throw new RuntimeException("AI 퀴즈 생성 실패: " + e.getMessage());
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