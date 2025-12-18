package im.swyp.teumteumeat.global.config.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // 날짜/시간 모듈 등록
        mapper.registerModule(new JavaTimeModule());

        // 날짜를 ISO-8601 형식 문자열로 직렬화 (timestamp 대신)
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // null 값 필드 제외
        mapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);

        // snake_case 네이밍 전략 적용
        // mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        // JSON Pretty Print 활성화
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        return mapper;
    }
}