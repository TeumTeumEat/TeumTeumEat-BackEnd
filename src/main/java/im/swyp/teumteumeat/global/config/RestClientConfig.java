package im.swyp.teumteumeat.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    @Value("${rest-client.connect-timeout}")
    private long CONNECT_TIMEOUT;

    @Value("${rest-client.read-timeout}")
    private long READ_TIMEOUT;

    // OpenAI Config
    @Value("${spring.ai.openai.api-key}")
    private String openAiApiKey;

    @Value("${spring.ai.openai.base-url:https://api.openai.com/v1}")
    private String openAiBaseUrl;

    @Bean
    public RestClient restClient() {
        return RestClient.builder()
                .requestFactory(customRequestFactory())
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Bean
    public RestClient openAiRestClient() {
        return RestClient.builder()
                .baseUrl(openAiBaseUrl)
                .requestFactory(customRequestFactory())
                .defaultHeader("Authorization", "Bearer " + openAiApiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    private ClientHttpRequestFactory customRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(CONNECT_TIMEOUT));
        factory.setReadTimeout(Duration.ofSeconds(READ_TIMEOUT));
        return factory;
    }
}