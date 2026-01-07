package im.swyp.teumteumeat.global.security;

import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppleUtil {

    private final ResourceLoader resourceLoader;

    @Value("${spring.apple.team-id}")
    private String teamId;

    @Value("${spring.apple.client-id}")
    private String clientId;

    @Value("${spring.apple.key-id}")
    private String keyId;

    @Value("${spring.apple.key-path}")
    private String keyPath;

    public String getClientId() {
        return clientId;
    }

    public String createClientSecret() {
        Date expirationDate = Date.from(LocalDateTime.now().plusDays(30).atZone(ZoneId.systemDefault()).toInstant());
        Date issuedAt = Date.from(LocalDateTime.now().minusMinutes(1).atZone(ZoneId.systemDefault()).toInstant());

        try {
            return Jwts.builder()
                    .header().keyId(keyId).and()
                    .issuer(teamId)
                    .audience().add("https://appleid.apple.com").and()
                    .subject(clientId)
                    .expiration(expirationDate)
                    .issuedAt(issuedAt)
                    .signWith(getPrivateKey(), Jwts.SIG.ES256)
                    .compact();
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            log.error("Error creating Apple client secret", e);
            throw new RuntimeException("Error creating Apple client secret", e);
        }
    }

    private PrivateKey getPrivateKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        String resourcePath = keyPath;
        if (!resourcePath.startsWith("classpath:") && !resourcePath.contains(":")) {
            resourcePath = "classpath:" + resourcePath;
        }
        InputStream inputStream = resourceLoader.getResource(resourcePath).getInputStream();
        byte[] bdata = FileCopyUtils.copyToByteArray(inputStream);
        String privateKeyString = new String(bdata, StandardCharsets.UTF_8);

        privateKeyString = privateKeyString.replaceAll("-----BEGIN PRIVATE KEY-----", "")
                .replaceAll("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");

        byte[] decodedKey = Base64.getDecoder().decode(privateKeyString);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedKey);
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        return keyFactory.generatePrivate(keySpec);
    }
}
