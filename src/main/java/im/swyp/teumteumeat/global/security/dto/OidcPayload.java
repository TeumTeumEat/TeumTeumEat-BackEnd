package im.swyp.teumteumeat.global.security.dto;

public record OidcPayload(
	/* issuer */
	String iss,
	/* client id */
	String aud,
	/* aouth provider account unique id */
	String sub,
	String email,
    String name
) {
}
