package im.swyp.teumteumeat.global.security.service;

import im.swyp.teumteumeat.global.common.CommonResponseCode;
import im.swyp.teumteumeat.global.exception.BaseException;
import im.swyp.teumteumeat.global.security.component.ApplePublicKeyGenerator;
import im.swyp.teumteumeat.global.security.constant.AuthResponseCode;
import im.swyp.teumteumeat.global.security.dto.ApplePublicKeyResponse;
import im.swyp.teumteumeat.global.security.token.JwtProvider;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppleAuthService {

    @Value("spring.apple.client-id")
    private String clientId;

    @Value("spring.apple.issuer")
    private String issuer;

    private final RestClient restClient;
    private final JwtProvider jwtProvider;
    private final ApplePublicKeyGenerator applePublicKeyGenerator;
    private static final String APPLE_PUBLIC_KEYS_URL = "https://appleid.apple.com/auth/keys";

    public Map<String, Object> verifyIdToken(String idToken) {
        try {
            // jwt 헤더를 파싱한다.
            Map<String, String> headers = jwtProvider.parseHeaders(idToken);
            // 공개키를 생성한다
            PublicKey publicKey = applePublicKeyGenerator.generatePublicKey(headers, getAppleAuthPublicKey());
            // 토큰의 서명을 검사하고 Claim 을 반환받는다.
            Claims tokenClaims = jwtProvider.parseSocialToken(idToken, publicKey);
            // iss 필드 검사
            if (!issuer.equals(tokenClaims.getIssuer())) {
                throw new BaseException(AuthResponseCode.INVALID_JWT_TOKEN);
            }
            // aud 필드 검사
            if (tokenClaims.getAudience() == null || !tokenClaims.getAudience().contains(clientId)) {
                throw new BaseException(AuthResponseCode.INVALID_JWT_TOKEN);
            }

            return new HashMap<>(tokenClaims);
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            throw new BaseException(CommonResponseCode.INTERNAL_SERVER_ERROR);
        }
    }

    private ApplePublicKeyResponse getAppleAuthPublicKey() {
        return restClient.get()
                .uri(APPLE_PUBLIC_KEYS_URL)
                .retrieve()
                .body(ApplePublicKeyResponse.class);
    }
}
