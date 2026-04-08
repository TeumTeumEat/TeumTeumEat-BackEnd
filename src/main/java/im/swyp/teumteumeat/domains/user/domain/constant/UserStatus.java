package im.swyp.teumteumeat.domains.user.domain.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserStatus {

    PENDING("이용약관 미동의"),
    ACTIVE("활성");

    private final String description;
}
