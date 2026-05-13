package im.swyp.teumteumeat.domains.notification.application.dto.request;

import im.swyp.teumteumeat.domains.notification.domain.constant.DeviceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DeviceTokenRequest(
        @NotBlank(message = "디바이스 토큰은 필수입니다.")
        @Schema(description = "디바이스 토큰", example = "yBXekEbKfu...")
        String token,

        @NotNull(message = "디바이스 종류는 필수입니다.")
        @Schema(description = "디바이스 종류", example = "IOS/ANDROID")
        DeviceType deviceType
) {
}
