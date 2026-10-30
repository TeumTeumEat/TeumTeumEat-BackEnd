package im.swyp.teumteumeat.global.security.component;

import im.swyp.teumteumeat.global.config.properties.JwtProperties;
import im.swyp.teumteumeat.global.security.properties.RefreshTokenCookieProperties;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 웹 로그인용 리프레시 토큰 쿠키 생성/만료.
 * HttpOnly로 스크립트 접근을 차단하며, 앱은 기존처럼 body로 리프레시 토큰을 주고받습니다.
 */
@Component
@RequiredArgsConstructor
public class RefreshTokenCookieProvider {

    public static final String COOKIE_NAME = "refresh_token";
    private static final String COOKIE_PATH = "/";

    private final RefreshTokenCookieProperties cookieProperties;
    private final JwtProperties jwtProperties;

    public void addCookie(HttpServletResponse response, String refreshToken) {
        Duration maxAge = Duration.ofMillis(jwtProperties.refreshToken().expirationTime());
        response.addHeader(HttpHeaders.SET_COOKIE, build(refreshToken, maxAge).toString());
    }

    public void expireCookie(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, build("", Duration.ZERO).toString());
    }

    private ResponseCookie build(String value, Duration maxAge) {
        return ResponseCookie.from(COOKIE_NAME, value)
                .httpOnly(true)
                .secure(cookieProperties.secure())
                .sameSite(cookieProperties.sameSite())
                .path(COOKIE_PATH)
                .maxAge(maxAge)
                .build();
    }
}
