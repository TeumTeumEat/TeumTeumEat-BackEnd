package im.swyp.teumteumeat.global.security.component;

import im.swyp.teumteumeat.global.common.CommonResponseCode;
import im.swyp.teumteumeat.global.exception.BaseException;
import im.swyp.teumteumeat.global.security.client.OidcClient;
import im.swyp.teumteumeat.global.security.client.apple.AppleOidcClient;
import im.swyp.teumteumeat.global.security.client.google.GoogleOidcClient;
import im.swyp.teumteumeat.global.security.client.kakao.KakaoOidcClient;
import im.swyp.teumteumeat.global.security.constant.SocialProvider;
import im.swyp.teumteumeat.global.security.dto.OidcPayload;
import im.swyp.teumteumeat.global.security.dto.OidcPublicKey;
import im.swyp.teumteumeat.global.security.dto.OidcPublicKeyResponse;
import im.swyp.teumteumeat.global.security.properties.OidcClientProperties;
import im.swyp.teumteumeat.global.security.properties.apple.AppleOidcProperties;
import im.swyp.teumteumeat.global.security.properties.google.GoogleOidcProperties;
import im.swyp.teumteumeat.global.security.properties.kakao.KakaoOidcProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class OAuthOidcHelper {
	private final JwtOidcProvider jwtOidcProvider;
	private final Map<SocialProvider, Map<OidcClient, OidcClientProperties>> oauthOidcClients;

	public OAuthOidcHelper(
		JwtOidcProvider jwtOidcProvider,
		KakaoOidcClient kakaoOidcClient,
		GoogleOidcClient googleOidcClient,
		AppleOidcClient appleOidcClient,
		KakaoOidcProperties kakaoOidcProperties,
		GoogleOidcProperties googleOidcProperties,
		AppleOidcProperties appleOidcProperties
	) {
		this.jwtOidcProvider = jwtOidcProvider;
		this.oauthOidcClients = Map.of(
            SocialProvider.KAKAO, Map.of(kakaoOidcClient, kakaoOidcProperties),
            SocialProvider.GOOGLE, Map.of(googleOidcClient, googleOidcProperties),
            SocialProvider.APPLE, Map.of(appleOidcClient, appleOidcProperties)
		);
	}

	/**
	 * Provider에 따라 Client와 Properties를 선택하고 Odic public key 정보를 가져와서 ID Token의 payload를 추출하는 메서드
	 *
	 * @param provider : {@link SocialProvider}
	 * @param idToken  : idToken
	 * @return OIDCDecodePayload : ID Token의 payload
	 */
	public OidcPayload getPayload(SocialProvider provider, String idToken) {
		OidcClient client = oauthOidcClients.get(provider).keySet().iterator().next();
		OidcClientProperties properties = oauthOidcClients.get(provider).values().iterator().next();
		OidcPublicKeyResponse response = client.getOidcPublicKey();
		return getPayloadFromIdToken(idToken, properties.getIssuer(), properties.getSecret(), response);
	}

	/**
	 * ID Token의 payload를 추출하는 메서드 <br/>
	 * OAuth 2.0 spec에 따라 ID Token의 유효성 검사 수행 <br/>
	 *
	 * @param idToken  : idToken
	 * @param iss      : ID Token을 발급한 provider의 URL
	 * @param aud      : ID Token이 발급된 앱의 앱 키
	 * @param response : 공개키 목록
	 * @return OidcPayload : ID Token의 payload
	 */
	private OidcPayload getPayloadFromIdToken(String idToken, String iss, String aud, OidcPublicKeyResponse response) {
		String kid = jwtOidcProvider.getKidFromUnsignedTokenHeader(idToken, iss, aud);
		OidcPublicKey key = response.getKeys().stream()
			.filter(k -> k.kid().equals(kid))
			.findFirst()
			.orElseThrow(() -> {
                log.error("일치하는 공개키를 찾을 수 없습니다.");
                return new BaseException(CommonResponseCode.INTERNAL_SERVER_ERROR);
            });
		return jwtOidcProvider.getOidcTokenBody(idToken, key.n(), key.e());
	}
}
