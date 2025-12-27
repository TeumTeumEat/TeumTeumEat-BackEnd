package im.swyp.teumteumeat.global.security.token;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import im.swyp.teumteumeat.domains.user.domain.constant.Role;
import im.swyp.teumteumeat.global.common.CommonResponseCode;
import im.swyp.teumteumeat.global.security.constant.SocialProvider;
import im.swyp.teumteumeat.domains.user.persistence.entity.UserEntity;
import im.swyp.teumteumeat.global.config.properties.JwtProperties;
import im.swyp.teumteumeat.global.exception.BaseException;
import im.swyp.teumteumeat.global.security.constant.AuthResponseCode;
import im.swyp.teumteumeat.global.security.service.CustomUserDetailsService;
import im.swyp.teumteumeat.domains.refreshtoken.domain.service.RefreshTokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.Jwts;
import lombok.Getter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

import static im.swyp.teumteumeat.global.common.Constants.*;

@Component
public class JwtProvider {

    @Getter
    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;
    private final ObjectMapper objectMapper;
    private final CustomUserDetailsService userDetailsService;
    private final RefreshTokenService refreshTokenService;

    JwtProvider(JwtProperties jwtProperties, ObjectMapper objectMapper, CustomUserDetailsService userDetailsService, RefreshTokenService refreshTokenService) {
        this.jwtProperties = jwtProperties;
        this.secretKey = new SecretKeySpec(jwtProperties.secret().getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
        this.objectMapper = objectMapper;
        this.userDetailsService = userDetailsService;
        this.refreshTokenService = refreshTokenService;
    }

    /**
     * 토큰 발급
     */
    public Token issueToken(UserEntity user) {
        TokenClaim tokenClaim = TokenClaim.builder()
                .socialProvider(user.getSocialProvider())
                .socialId(user.getSocialId())
                .role(user.getRole())
                .build();

        String accessToken = generateAccessToken(tokenClaim);
        String refreshToken = generateRefreshToken(tokenClaim);

        refreshTokenService.saveRefreshToken(refreshToken, user.getSocialProvider(), user.getSocialId());

        return Token.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * Access Token 발급
     */
    public String generateAccessToken(TokenClaim tokenClaim, long expirationTime) {
        return Jwts.builder()
                .claim(CLAIM_NAME_TOKEN_TYPE, CLAIM_VALUE_ACCESS_TOKEN)
                .subject(tokenClaim.socialProvider() + DELIMITER + tokenClaim.socialId())
                .claim(CLAIM_NAME_ROLE, tokenClaim.role().getKey())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(secretKey)
                .compact();
    }

    public String generateAccessToken(TokenClaim tokenClaim) {
        return generateAccessToken(tokenClaim, jwtProperties.accessToken().expirationTime());
    }

    /**
     * Access Token 재발급
     * todo 보완 필요
     */
    public String reissueAccessToken(String refreshToken) {
        Claims claims = parseServerToken(refreshToken);
        String uniqueId = claims.getSubject();
        String[] parts = uniqueId.split(DELIMITER);
        SocialProvider socialProvider = SocialProvider.valueOf(parts[0]);
        String socialId = parts[1];

        TokenClaim tokenClaim = TokenClaim.builder()
                .socialProvider(socialProvider)
                .socialId(socialId)
                .role(Role.USER)
                .build();

        return generateAccessToken(tokenClaim);
    }

    /**
     * Refresh Token 발급
     */
    public String generateRefreshToken(TokenClaim tokenClaim, long expirationTime) {
        return Jwts.builder()
                .claim(CLAIM_NAME_TOKEN_TYPE, CLAIM_VALUE_REFRESH_TOKEN)
                .subject(tokenClaim.socialProvider() + DELIMITER + tokenClaim.socialId())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(secretKey)
                .compact();
    }

    public String generateRefreshToken(TokenClaim tokenClaim) {
        return generateRefreshToken(tokenClaim, jwtProperties.refreshToken().expirationTime());
    }

    /**
     * 토큰으로 인증 객체를 생성하여 반환
     */
    public Authentication getAuthentication(String token) {
        Claims claims = parseServerToken(token);
        String uniqueId = claims.getSubject();
        UserDetails userDetails = userDetailsService.loadUserByUsername(uniqueId);

        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    /**
     * 헤더 파싱
     */
    public Map<String, String> parseHeaders(String token) {
        try {
            String header = token.split("\\.")[0];
            return objectMapper.readValue(decodeHeader(header), new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new BaseException(AuthResponseCode.INVALID_JWT_TOKEN);
        }
    }

    /**
     * Apple 로그인에서 이용
     */
    public Claims parseSocialToken(String token, PublicKey publicKey) {
        return getClaims(token, publicKey);
    }

    /**
     * 토큰 유효성 검증 후 Claims(Payload) 반환
     */
    private Claims parseServerToken(String token) {
        //todo RedisTokenBlackList
        return getClaims(token, secretKey);
    }

    private Claims getClaims(String token, Key key) {
        JwtParserBuilder parserBuilder = Jwts.parser();

        if (key instanceof SecretKey sKey) {
            parserBuilder.verifyWith(sKey);
        } else if (key instanceof PublicKey pKey) {
            parserBuilder.verifyWith(pKey);
        } else {
            throw new BaseException(CommonResponseCode.INTERNAL_SERVER_ERROR);
        }

        try {
            return parserBuilder.build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new BaseException(AuthResponseCode.EXPIRED_JWT_TOKEN);
        } catch (Exception e) {
            throw new BaseException(AuthResponseCode.INVALID_JWT_TOKEN);
        }
    }

    private String decodeHeader(String token) {
        return new String(Base64.getDecoder().decode(token), StandardCharsets.UTF_8);
    }
}