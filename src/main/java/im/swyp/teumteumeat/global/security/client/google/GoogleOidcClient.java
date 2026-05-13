package im.swyp.teumteumeat.global.security.client.google;

import im.swyp.teumteumeat.global.security.client.AbstractOidcClient;
import im.swyp.teumteumeat.global.security.dto.OidcPublicKeyResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class GoogleOidcClient extends AbstractOidcClient {

    private final String googleJwksUrl;

    public GoogleOidcClient(
            RestClient restClient,
            @Value("${oauth2.client.provider.google.jwks-uri}") String googleIssuer) {
        super(restClient);
        this.googleJwksUrl = googleIssuer + "/oauth2/v3/certs";
    }

    @Cacheable(value = "GoogleOauth", cacheManager = "oidcCacheManager")
    public OidcPublicKeyResponse getOidcPublicKey() {
        return fetchKey(googleJwksUrl);
    }
}
