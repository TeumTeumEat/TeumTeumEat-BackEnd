package im.swyp.teumteumeat.global.security.client;

import im.swyp.teumteumeat.global.common.CommonResponseCode;
import im.swyp.teumteumeat.global.exception.BaseException;
import im.swyp.teumteumeat.global.security.dto.OidcPublicKeyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;

@RequiredArgsConstructor
public abstract class AbstractOidcClient implements OidcClient {

    protected final RestClient restClient;

    protected OidcPublicKeyResponse fetchKey(String url) {
        return restClient.get()
                .uri(url)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    throw new BaseException(CommonResponseCode.INTERNAL_SERVER_ERROR);
                })
                .body(OidcPublicKeyResponse.class);
    }
}