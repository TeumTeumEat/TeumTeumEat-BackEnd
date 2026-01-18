package im.swyp.teumteumeat.global.security.token;

import im.swyp.teumteumeat.domains.user.domain.constant.Role;
import im.swyp.teumteumeat.global.config.properties.JwtProperties;
import im.swyp.teumteumeat.global.exception.BaseException;
import im.swyp.teumteumeat.global.security.constant.AuthResponseCode;
import im.swyp.teumteumeat.global.security.service.CustomUserDetailsService;
import im.swyp.teumteumeat.domains.refreshtoken.domain.service.RefreshTokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import lombok.Getter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static im.swyp.teumteumeat.global.common.Constants.*;

@Component
public class JwtProvider {

    @Getter
    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;
    private final CustomUserDetailsService userDetailsService;
    private final RefreshTokenService refreshTokenService;

    JwtProvider(JwtProperties jwtProperties, CustomUserDetailsService userDetailsService, RefreshTokenService refreshTokenService) {
        this.jwtProperties = jwtProperties;
        this.secretKey = new SecretKeySpec(jwtProperties.secret().getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
        this.userDetailsService = userDetailsService;
        this.refreshTokenService = refreshTokenService;
    }

    /**
     * 토큰 발급
     */
    public Token issueToken(Long userId, Role role) {
        TokenClaim tokenClaim = TokenClaim.builder()
                .userId(userId)
                .role(role)
                .build();

        String accessToken = generateAccessToken(tokenClaim);
        String refreshToken = generateRefreshToken(tokenClaim);

        long ttlInSeconds = jwtProperties.refreshToken().expirationTime() / 1000;
        refreshTokenService.saveRefreshToken(userId, refreshToken, ttlInSeconds);

        return Token.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    // 실제 서비스에서 사용하는 메인 API
    public String generateAccessToken(TokenClaim tokenClaim) {
        return generateAccessToken(tokenClaim, jwtProperties.accessToken().expirationTime());
    }

    public String generateRefreshToken(TokenClaim tokenClaim) {
        return generateRefreshToken(tokenClaim, jwtProperties.refreshToken().expirationTime());
    }

    // 테스트 환경을 위해 expirationTime을 값으로 받음
    public String generateAccessToken(TokenClaim tokenClaim, long expirationTime) {
        return generateToken(tokenClaim, CLAIM_VALUE_ACCESS_TOKEN, expirationTime);
    }

    public String generateRefreshToken(TokenClaim tokenClaim, long expirationTime) {
        return generateToken(tokenClaim, CLAIM_VALUE_REFRESH_TOKEN, expirationTime);
    }

    /**
     * 토큰 발급
     */
    private String generateToken(TokenClaim tokenClaim, String tokenType, long expirationTime) {
        return Jwts.builder()
                .claim(CLAIM_NAME_TOKEN_TYPE, tokenType)
                .claim(CLAIM_NAME_ROLE, tokenClaim.role().getKey())
                .subject(String.valueOf(tokenClaim.userId()))
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(secretKey)
                .compact();
    }

    /**
     * Access Token 재발급
     */
    public String reissueAccessToken(String refreshToken) {
        if (!refreshTokenService.existRefreshToken(refreshToken)) {
            throw new BaseException(AuthResponseCode.INVALID_JWT_TOKEN);
        }

        Claims claims = parseClaims(refreshToken);
        String userIdStr = claims.getSubject();
        Long userId = Long.parseLong(userIdStr);

        String roleKey = claims.get(CLAIM_NAME_ROLE, String.class);
        Role role = Role.fromKey(roleKey);

        TokenClaim tokenClaim = TokenClaim.builder()
                .userId(userId)
                .role(role)
                .build();

        return generateAccessToken(tokenClaim);
    }

    /**
     * 토큰으로 인증 객체를 생성하여 반환
     */
    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);
        String userId = claims.getSubject();
        UserDetails userDetails = userDetailsService.loadUserByUsername(userId);

        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    /**
     * 토큰 유효성 검증 후 Claims(Payload) 반환
     */
    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new BaseException(AuthResponseCode.EXPIRED_JWT_TOKEN);
        } catch (RuntimeException e) {
            throw new BaseException(AuthResponseCode.INVALID_JWT_TOKEN);
        }
    }

    public void removeRefreshToken(Long userId, String refreshToken) {
        refreshTokenService.deleteRefreshToken(userId, refreshToken);
    }
}