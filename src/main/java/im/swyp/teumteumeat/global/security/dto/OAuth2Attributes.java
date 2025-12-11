package im.swyp.teumteumeat.global.security.dto;

import im.swyp.teumteumeat.global.security.constant.SocialProvider;
import im.swyp.teumteumeat.global.exception.BaseException;
import im.swyp.teumteumeat.global.security.constant.AuthResponseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Getter
@RequiredArgsConstructor
public abstract class OAuth2Attributes {

    private final Map<String, Object> attributes;

    private final String userNameAttributeKey;

    abstract public SocialProvider getProvider();

    abstract public String getName();

    abstract public String getEmail();

    public String getProviderId() {
        return String.valueOf(attributes.get(userNameAttributeKey));
    }

    public static OAuth2Attributes of(final String registrationId, final Map<String, Object> attributes, final String userNameAttributeKey) {
        if (SocialProvider.GOOGLE.getRegistrationId().equals(registrationId)) {
            return new GoogleOAuth2Attributes(attributes, userNameAttributeKey);
        }

        if (SocialProvider.KAKAO.getRegistrationId().equals(registrationId)) {
            return new KakaoOAuth2Attributes(attributes, "id");
        }

        //todo APPLE

        throw new BaseException(AuthResponseCode.NOT_SUPPORTED_SOCIAL_PROVIDER);
    }
}