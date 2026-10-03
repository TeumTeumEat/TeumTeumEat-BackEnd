package im.swyp.teumteumeat.global.security.controller;

import im.swyp.teumteumeat.domains.user.domain.constant.Role;
import im.swyp.teumteumeat.global.config.properties.JwtProperties;
import im.swyp.teumteumeat.global.security.component.RefreshTokenCookieProvider;
import im.swyp.teumteumeat.global.security.dto.CustomUserDetails;
import im.swyp.teumteumeat.global.security.properties.RefreshTokenCookieProperties;
import im.swyp.teumteumeat.global.security.usecase.OAuth2UseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AuthControllerTest {

    private OAuth2UseCase oAuth2UseCase;
    private AuthController authController;

    @BeforeEach
    void setUp() {
        oAuth2UseCase = mock(OAuth2UseCase.class);
        JwtProperties jwtProperties = new JwtProperties(
                "secret",
                new JwtProperties.AccessTokenProperties(3_600_000L),
                new JwtProperties.RefreshTokenProperties(1_209_600_000L, 3));
        RefreshTokenCookieProvider refreshTokenCookieProvider = new RefreshTokenCookieProvider(
                new RefreshTokenCookieProperties(true, "None"), jwtProperties);
        authController = new AuthController(oAuth2UseCase, refreshTokenCookieProvider);
    }

    @Test
    @DisplayName("쿠키로 로그아웃하면 쿠키의 리프레시 토큰을 폐기하고 쿠키를 만료시킨다.")
    void logOutWithCookieRemovesTokenAndExpiresCookie() {
        // given
        CustomUserDetails user = new CustomUserDetails(1L, Role.ADMIN, List.of());
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        authController.logOut("cookie-refresh", null, user, response);

        // then
        verify(oAuth2UseCase).logOut(1L, "cookie-refresh");
        assertThat(response.getHeader(HttpHeaders.SET_COOKIE))
                .contains("refresh_token=")
                .contains("Max-Age=0");
    }

    @Test
    @DisplayName("쿠키 없이 로그아웃해도 리프레시 토큰 쿠키를 만료시킨다.")
    void logOutWithoutCookieStillExpiresCookie() {
        // given
        CustomUserDetails user = new CustomUserDetails(1L, Role.USER, List.of());
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        authController.logOut(null, "param-refresh", user, response);

        // then
        verify(oAuth2UseCase).logOut(1L, "param-refresh");
        assertThat(response.getHeader(HttpHeaders.SET_COOKIE)).contains("Max-Age=0");
    }
}
