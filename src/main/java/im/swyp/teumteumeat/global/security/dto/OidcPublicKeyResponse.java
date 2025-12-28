package im.swyp.teumteumeat.global.security.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class OidcPublicKeyResponse {
	List<OidcPublicKey> keys;
}
