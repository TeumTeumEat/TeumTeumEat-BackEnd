package im.swyp.teumteumeat.global.security.client.kakao;

import im.swyp.teumteumeat.global.security.client.AbstractOidcClient;
import im.swyp.teumteumeat.global.security.dto.OidcPublicKeyResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class KakaoOidcClient extends AbstractOidcClient {

    private final String kakaoJwksUrl;

    public KakaoOidcClient(
            RestClient restClient,
            @Value("${oauth2.client.provider.kakao.jwks-uri}") String kakaoIssuer) {
        super(restClient);
        this.kakaoJwksUrl = kakaoIssuer + "/.well-known/jwks.json";
    }

    @Cacheable(value = "KakaoOauth", cacheManager = "oidcCacheManager")
    public OidcPublicKeyResponse getOidcPublicKey() {
        return fetchKey(kakaoJwksUrl);
    }
}
