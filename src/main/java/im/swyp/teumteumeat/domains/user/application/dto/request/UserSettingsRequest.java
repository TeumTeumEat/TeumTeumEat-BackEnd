package im.swyp.teumteumeat.domains.user.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserSettingsRequest(

        @Schema(description = "(Nullable) 푸쉬 알림 설정", example = "true")
        Boolean pushEnabled
) {
}
