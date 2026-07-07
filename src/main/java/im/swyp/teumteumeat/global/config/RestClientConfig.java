package im.swyp.teumteumeat.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
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

    @Bean
    public RestClient restClient() {
        return RestClient.builder()
                .requestFactory(customRequestFactory())
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    // Spring AI의 OpenAiAutoConfiguration이 이 빌더를 주입받아 OpenAI 호출에 사용하므로
    // 커스텀 타임아웃이 LLM 호출에도 적용된다. 빌더는 가변 객체라 prototype으로 선언한다.
    @Bean
    @Scope("prototype")
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder()
                .requestFactory(customRequestFactory());
    }

    private ClientHttpRequestFactory customRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(CONNECT_TIMEOUT));
        factory.setReadTimeout(Duration.ofSeconds(READ_TIMEOUT));
        return factory;
    }
}
