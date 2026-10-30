package im.swyp.teumteumeat.global.security.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cookie.refresh-token")
public record RefreshTokenCookieProperties(
        boolean secure,
        String sameSite
) {}
