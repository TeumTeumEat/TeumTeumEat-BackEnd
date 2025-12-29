package im.swyp.teumteumeat.domains.user.application.dto.request;

public record UserWithdrawalRequest(
        String appleAuthorizationCode,
        String googleAccessToken) {
}
