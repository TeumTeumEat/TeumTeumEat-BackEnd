package im.swyp.teumteumeat.global.security.token;

import im.swyp.teumteumeat.domains.user.domain.constant.Role;
import lombok.Builder;

@Builder
public record TokenClaim(
        Long userId,
        Role role
) {
}