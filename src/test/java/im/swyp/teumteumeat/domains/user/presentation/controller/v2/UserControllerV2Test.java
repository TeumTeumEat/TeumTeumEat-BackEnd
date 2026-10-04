package im.swyp.teumteumeat.domains.user.presentation.controller.v2;

import im.swyp.teumteumeat.global.common.ApiResponse;
import im.swyp.teumteumeat.global.config.properties.JwtProperties;
import im.swyp.teumteumeat.global.security.component.RefreshTokenCookieProvider;
import im.swyp.teumteumeat.global.security.dto.ReissueRequest;
import im.swyp.teumteumeat.global.security.properties.RefreshTokenCookieProperties;
import im.swyp.teumteumeat.global.security.token.JwtProvider;
import im.swyp.teumteumeat.global.security.token.TokenResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class UserControllerV2Test {

    private static final long REFRESH_TOKEN_EXPIRATION_MILLIS = 1_209_600_000L;

    private JwtProvider jwtProvider;
    private UserControllerV2 userControllerV2;

    @BeforeEach
    void setUp() {
        jwtProvider = mock(JwtProvider.class);
        JwtProperties jwtProperties = new JwtProperties(
                "secret",
                new JwtProperties.AccessTokenProperties(3_600_000L),
                new JwtProperties.RefreshTokenProperties(REFRESH_TOKEN_EXPIRATION_MILLIS, 3));
        RefreshTokenCookieProvider refreshTokenCookieProvider = new RefreshTokenCookieProvider(
                new RefreshTokenCookieProperties(true, "None"), jwtProperties);
        userControllerV2 = new UserControllerV2(jwtProvider, refreshTokenCookieProvider);
    }

    @Test
    @DisplayName("쿠키로 재발급을 요청하면 회전된 리프레시 토큰은 쿠키로만 내려가고 응답 body에는 포함되지 않는다.")
    void reissueWithCookieRotatesRefreshTokenOnlyInCookie() {
        // given
        given(jwtProvider.reissueTokens("old-refresh"))
                .willReturn(new TokenResponse("new-access", "new-refresh"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        ResponseEntity<ApiResponse<TokenResponse>> result =
                userControllerV2.tokenReissue("old-refresh", null, response);

        // then
        assertThat(result.getBody().data().accessToken()).isEqualTo("new-access");
        assertThat(result.getBody().data().refreshToken()).isNull();
        assertThat(response.getHeader(HttpHeaders.SET_COOKIE))
                .contains("refresh_token=new-refresh")
                .contains("HttpOnly")
                .contains("Secure")
                .contains("SameSite=None")
                .contains("Max-Age=" + REFRESH_TOKEN_EXPIRATION_MILLIS / 1000);
    }

    @Test
    @DisplayName("쿠키로 재발급을 요청했지만 리프레시 토큰이 회전되지 않으면 쿠키를 새로 내려주지 않는다.")
    void reissueWithCookieWithoutRotationDoesNotSetCookie() {
        // given
        given(jwtProvider.reissueTokens("refresh"))
                .willReturn(new TokenResponse("new-access", null));
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        ResponseEntity<ApiResponse<TokenResponse>> result =
                userControllerV2.tokenReissue("refresh", null, response);

        // then
        assertThat(result.getBody().data().accessToken()).isEqualTo("new-access");
        assertThat(result.getBody().data().refreshToken()).isNull();
        assertThat(response.getHeader(HttpHeaders.SET_COOKIE)).isNull();
    }

    @Test
    @DisplayName("body로 재발급을 요청하면(앱) 회전된 리프레시 토큰을 기존처럼 body로 내려주고 쿠키는 내려주지 않는다.")
    void reissueWithBodyReturnsRefreshTokenInBody() {
        // given
        given(jwtProvider.reissueTokens("old-refresh"))
                .willReturn(new TokenResponse("new-access", "new-refresh"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        ResponseEntity<ApiResponse<TokenResponse>> result =
                userControllerV2.tokenReissue(null, new ReissueRequest("old-refresh"), response);

        // then
        assertThat(result.getBody().data().accessToken()).isEqualTo("new-access");
        assertThat(result.getBody().data().refreshToken()).isEqualTo("new-refresh");
        assertThat(response.getHeader(HttpHeaders.SET_COOKIE)).isNull();
    }
}
