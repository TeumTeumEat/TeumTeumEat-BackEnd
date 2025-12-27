package im.swyp.teumteumeat.global.security.dto;

import im.swyp.teumteumeat.global.exception.BaseException;
import im.swyp.teumteumeat.global.security.constant.AuthResponseCode;

import java.util.List;

public record ApplePublicKeyResponse(List<ApplePublicKey> keys) {
    public ApplePublicKey getMatchedKey(String kid, String alg) {
        return keys.stream()
                .filter(key -> key.kid().equals(kid) && key.alg().equals(alg))
                .findAny()
                .orElseThrow(() -> new BaseException(AuthResponseCode.INVALID_JWT_TOKEN));
    }
}