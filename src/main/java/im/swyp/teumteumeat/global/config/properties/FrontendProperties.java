package im.swyp.teumteumeat.global.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "frontend")
public record FrontendProperties(
        String baseUrl,
        String mainPage,
        String loginFailPage,
        String localUrl,
        String localSecureUrl
) {

}