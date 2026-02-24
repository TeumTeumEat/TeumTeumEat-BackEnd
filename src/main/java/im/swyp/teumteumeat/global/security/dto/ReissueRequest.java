package im.swyp.teumteumeat.global.security.dto;

import jakarta.validation.constraints.NotBlank;

public record ReissueRequest(
    @NotBlank
    String refreshToken
) {
}