package im.swyp.teumteumeat.domains.user.application.dto.response;

import im.swyp.teumteumeat.global.security.constant.SocialProvider;
import lombok.Builder;

@Builder
public record AccountInfoResponse(
        SocialProvider socialProvider,
        String email
) {
}
