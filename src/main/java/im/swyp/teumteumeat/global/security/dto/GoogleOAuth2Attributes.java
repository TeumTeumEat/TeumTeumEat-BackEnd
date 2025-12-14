package im.swyp.teumteumeat.global.security.dto;

import im.swyp.teumteumeat.global.security.constant.SocialProvider;

import java.util.Map;

public class GoogleOAuth2Attributes extends OAuth2Attributes {

    public GoogleOAuth2Attributes(final Map<String, Object> attributes, final String userNameAttributeKey) {
        super(attributes, userNameAttributeKey);
    }

    @Override
    public SocialProvider getProvider() {
        return SocialProvider.GOOGLE;
    }

    @Override
    public String getEmail() {
        return String.valueOf(getAttributes().get("email"));
    }

    @Override
    public String getName() {
        return String.valueOf(getAttributes().get("name"));
    }
}