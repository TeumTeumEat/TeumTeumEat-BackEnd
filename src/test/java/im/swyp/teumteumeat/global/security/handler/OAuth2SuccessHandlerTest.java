package im.swyp.teumteumeat.global.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import im.swyp.teumteumeat.domains.user.domain.constant.Role;
import im.swyp.teumteumeat.domains.user.domain.service.UserService;
import im.swyp.teumteumeat.global.config.properties.JwtProperties;
import im.swyp.teumteumeat.global.security.component.OAuth2ResponseHandler;
import im.swyp.teumteumeat.global.security.component.RefreshTokenCookieProvider;
import im.swyp.teumteumeat.global.security.dto.CustomUserDetails;
import im.swyp.teumteumeat.global.security.properties.OAuth2RedirectProperties;
import im.swyp.teumteumeat.global.security.properties.RefreshTokenCookieProperties;
import im.swyp.teumteumeat.global.security.repository.HttpCookieOAuth2AuthorizationRequestRepository;
import im.swyp.teumteumeat.global.security.token.JwtProvider;
import im.swyp.teumteumeat.global.security.token.Token;
import im.swyp.teumteumeat.global.utils.CookieUtils;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class OAuth2SuccessHandlerTest {

    private static final String ADMIN_CALLBACK_URI = "https://admin.teumteumeat.co.kr/oauth/callback";

    private JwtProvider jwtProvider;
    private OAuth2SuccessHandler oAuth2SuccessHandler;

    @BeforeEach
    void setUp() {
        jwtProvider = mock(JwtProvider.class);
        ObjectMapper objectMapper = new ObjectMapper();
        JwtProperties jwtProperties = new JwtProperties(
                "secret",
                new JwtProperties.AccessTokenProperties(3_600_000L),
                new JwtProperties.RefreshTokenProperties(1_209_600_000L, 3));
        RefreshTokenCookieProvider refreshTokenCookieProvider = new RefreshTokenCookieProvider(
                new RefreshTokenCookieProperties(true, "None"), jwtProperties);
        OAuth2ResponseHandler oAuth2ResponseHandler = new OAuth2ResponseHandler(
                new CookieUtils(objectMapper),
                objectMapper,
                new OAuth2RedirectProperties(List.of("https://admin.teumteumeat.co.kr")),
                refreshTokenCookieProvider);
        oAuth2SuccessHandler = new OAuth2SuccessHandler(
                jwtProvider,
                mock(OAuth2AuthorizedClientRepository.class),
                mock(UserService.class),
                oAuth2ResponseHandler);
    }

    @Test
    @DisplayName("웹 소셜 로그인에 성공하면 리프레시 토큰은 HttpOnly 쿠키로 심고, 리다이렉트 URL에는 토큰을 노출하지 않는다.")
    void webLoginSetsRefreshTokenCookieWithoutTokenInRedirectUrl() throws Exception {
        // given
        given(jwtProvider.issueToken(1L, Role.ADMIN))
                .willReturn(new Token("access", "refresh"));
        Authentication authentication = mock(Authentication.class);
        given(authentication.getPrincipal())
                .willReturn(new CustomUserDetails(1L, Role.ADMIN, List.of()));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie(
                HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME, ADMIN_CALLBACK_URI));
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        oAuth2SuccessHandler.onAuthenticationSuccess(request, response, authentication);

        // then
        assertThat(response.getRedirectedUrl()).isEqualTo(ADMIN_CALLBACK_URI);
        assertThat(response.getHeader(HttpHeaders.SET_COOKIE))
                .contains("refresh_token=refresh")
                .contains("HttpOnly");
    }
}
