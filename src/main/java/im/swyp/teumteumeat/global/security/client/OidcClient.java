package im.swyp.teumteumeat.global.security.client;

import im.swyp.teumteumeat.global.security.dto.OidcPublicKeyResponse;

public interface OidcClient {
	OidcPublicKeyResponse getOidcPublicKey();
}
