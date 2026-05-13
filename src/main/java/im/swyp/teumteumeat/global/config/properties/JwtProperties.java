package im.swyp.teumteumeat.global.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        String secret,
        AccessTokenProperties accessToken,
        RefreshTokenProperties refreshToken
) {

    @ConfigurationProperties(prefix = "access-token")
    public record AccessTokenProperties(
            long expirationTime
    ) { }

    @ConfigurationProperties(prefix = "refresh-token")
    public record RefreshTokenProperties(
            long expirationTime,
            long reissueLimitDays
    ) { }
}