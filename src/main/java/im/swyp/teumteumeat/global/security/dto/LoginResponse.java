package im.swyp.teumteumeat.global.security.dto;

import lombok.Builder;

@Builder
public record LoginResponse(
        String accessToken,

        String refreshToken,

        boolean isOnboardingCompleted
) {
}
