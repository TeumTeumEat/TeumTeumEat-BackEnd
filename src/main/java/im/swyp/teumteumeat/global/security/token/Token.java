package im.swyp.teumteumeat.global.security.token;

import lombok.Builder;

@Builder
public record Token(
        String accessToken,
        String refreshToken
) {
}