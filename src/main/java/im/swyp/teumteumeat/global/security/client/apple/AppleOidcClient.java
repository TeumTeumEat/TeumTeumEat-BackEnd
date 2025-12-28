package im.swyp.teumteumeat.global.security.client.apple;

import im.swyp.teumteumeat.global.security.client.AbstractOidcClient;
import im.swyp.teumteumeat.global.security.dto.OidcPublicKeyResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AppleOidcClient extends AbstractOidcClient {

    private final String appleJwksUrl;

    public AppleOidcClient(
            RestClient restClient,
            @Value("${oauth2.client.provider.apple.jwks-uri}") String appleIssuer) {
        super(restClient);
        this.appleJwksUrl = appleIssuer + "/auth/keys";
    }

    @Cacheable(value = "AppleOauth", cacheManager = "oidcCacheManager")
    public OidcPublicKeyResponse getOidcPublicKey() {
        return fetchKey(appleJwksUrl);
    }
}
