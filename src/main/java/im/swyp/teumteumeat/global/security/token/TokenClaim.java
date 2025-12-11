package im.swyp.teumteumeat.global.security.token;

import im.swyp.teumteumeat.domains.user.domain.constant.Role;
import im.swyp.teumteumeat.global.security.constant.SocialProvider;
import lombok.Builder;

@Builder
public record TokenClaim(
        SocialProvider socialProvider,
        String socialId,
        Role role
) {
}