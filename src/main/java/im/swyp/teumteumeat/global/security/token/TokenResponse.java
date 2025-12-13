package im.swyp.teumteumeat.global.security.token;

public record TokenResponse(
        String accessToken,
        String refreshToken
) {
}