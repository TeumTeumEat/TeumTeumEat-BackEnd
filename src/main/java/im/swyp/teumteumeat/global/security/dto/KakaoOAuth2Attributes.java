package im.swyp.teumteumeat.global.security.dto;

import im.swyp.teumteumeat.global.security.constant.SocialProvider;

import java.util.Map;

public class KakaoOAuth2Attributes extends OAuth2Attributes {

    private final Map<String, Object> kakaoAttributes;

    public KakaoOAuth2Attributes(final Map<String, Object> attributes, final String userNameAttributeKey) {
        super(attributes, userNameAttributeKey);
        kakaoAttributes = (Map<String, Object>) attributes.get("kakao_account");
    }

    @Override
    public SocialProvider getProvider() {
        return SocialProvider.KAKAO;
    }

    @Override
    public String getEmail() {
        return String.valueOf(kakaoAttributes.get("email"));
    }

    @Override
    public String getName() {
        return String.valueOf(((Map<String, Object>) kakaoAttributes.get("profile")).get("nickname"));
    }
}