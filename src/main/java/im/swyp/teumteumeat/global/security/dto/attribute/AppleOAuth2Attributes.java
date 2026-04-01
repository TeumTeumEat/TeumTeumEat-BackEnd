package im.swyp.teumteumeat.global.security.dto.attribute;

import im.swyp.teumteumeat.global.security.constant.SocialProvider;

import java.util.Map;

public class AppleOAuth2Attributes extends OAuth2Attributes {

    public AppleOAuth2Attributes(final Map<String, Object> attributes, final String userNameAttributeKey) {
        super(attributes, userNameAttributeKey);
    }

    @Override
    public SocialProvider getProvider() {
        return SocialProvider.APPLE;
    }

    @Override
    public String getEmail() {
        return String.valueOf(getAttributes().get("email"));
    }

    @Override
    public String getName() {
        // Apple only returns the user's name on the first login request.
        // Usually, we default directly to "Apple User" or rely on the client to send it
        // separately if needed.
        return "Apple User";
    }
}
