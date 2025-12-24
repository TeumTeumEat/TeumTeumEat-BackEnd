package im.swyp.teumteumeat.domains.user.application.dto.response;

import lombok.Builder;

@Builder
public record UserSettingsResponse(
        Boolean pushEnabled
) {
}
