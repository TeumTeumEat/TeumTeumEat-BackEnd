package im.swyp.teumteumeat.global.security.properties.apple;

import im.swyp.teumteumeat.global.security.properties.OidcClientProperties;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "oauth2.client.provider.apple")
public class AppleOidcProperties implements OidcClientProperties {
	private final String jwksUri;
	private final String secret;

	@Override
	public String getIssuer() {
		return jwksUri;
	}
}

