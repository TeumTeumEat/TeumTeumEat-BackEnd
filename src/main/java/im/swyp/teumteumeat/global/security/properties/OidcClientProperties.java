package im.swyp.teumteumeat.global.security.properties;

public interface OidcClientProperties {
	String getJwksUri();

	String getSecret();

	String getIssuer();
}
