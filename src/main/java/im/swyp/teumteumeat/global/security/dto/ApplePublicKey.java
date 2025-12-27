package im.swyp.teumteumeat.global.security.dto;

public record ApplePublicKey(String kty, String kid, String alg, String n, String e) {
}