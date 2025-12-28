package im.swyp.teumteumeat.global.security.dto;

public record OidcPublicKey(
	String kid,
	String kty,
	String alg,
	String use,
	String n,
	String e
) {
}
