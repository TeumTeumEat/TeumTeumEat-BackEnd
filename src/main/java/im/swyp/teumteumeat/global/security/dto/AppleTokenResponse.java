package im.swyp.teumteumeat.global.security.dto;

import com.fasterxml.jackson.databind.EnumNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(SnakeCaseStrategy.class)
public record AppleTokenResponse(
        String accessToken,
        String tokenType,
        Long expiresIn,
        String refreshToken,
        String idToken) {
}
