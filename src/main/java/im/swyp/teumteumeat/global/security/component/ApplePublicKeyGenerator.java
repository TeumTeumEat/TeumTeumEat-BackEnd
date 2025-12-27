package im.swyp.teumteumeat.global.security.component;

import im.swyp.teumteumeat.global.exception.BaseException;
import im.swyp.teumteumeat.global.security.constant.AuthResponseCode;
import im.swyp.teumteumeat.global.security.dto.ApplePublicKey;
import im.swyp.teumteumeat.global.security.dto.ApplePublicKeyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ApplePublicKeyGenerator {

    public PublicKey generatePublicKey(Map<String, String> tokenHeaders,
                                       ApplePublicKeyResponse applePublicKeys) {
        try {
            ApplePublicKey publicKey = applePublicKeys.getMatchedKey(tokenHeaders.get("kid"),
                    tokenHeaders.get("alg"));
            return getPublicKey(publicKey);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new BaseException(AuthResponseCode.INVALID_JWT_TOKEN);
        }
    }

    private PublicKey getPublicKey(ApplePublicKey publicKey)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] nBytes = Base64.getUrlDecoder().decode(publicKey.n());
        byte[] eBytes = Base64.getUrlDecoder().decode(publicKey.e());
        RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(new BigInteger(1, nBytes),
                new BigInteger(1, eBytes));
        KeyFactory keyFactory = KeyFactory.getInstance(publicKey.kty());
        return keyFactory.generatePublic(publicKeySpec);
    }
}