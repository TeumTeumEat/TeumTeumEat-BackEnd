package im.swyp.teumteumeat.global.security.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SocialProvider {

    KAKAO("kakao"),
    GOOGLE("google"),
    APPLE("apple"),
    ;

    private final String registrationId;
}