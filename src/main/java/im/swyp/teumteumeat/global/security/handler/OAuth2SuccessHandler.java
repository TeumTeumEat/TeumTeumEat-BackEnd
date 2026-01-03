package im.swyp.teumteumeat.global.security.handler;

import im.swyp.teumteumeat.domains.user.domain.service.UserService;
import im.swyp.teumteumeat.global.config.properties.FrontendProperties;
import im.swyp.teumteumeat.global.security.dto.CustomUserDetails;
import im.swyp.teumteumeat.global.security.token.JwtProvider;
import im.swyp.teumteumeat.global.security.token.Token;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/**
 * 인증 객체를 기반으로 사용자에게 다음 단계를 제공
 */
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final FrontendProperties frontendProperties; // 웹 테스트용
    private final OAuth2AuthorizedClientRepository oAuth2AuthorizedClientRepository;
    private final UserService userService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException {
        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        Long userId = principal.getUserId();

        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
            OAuth2AuthorizedClient client = oAuth2AuthorizedClientRepository.loadAuthorizedClient(
                    oauthToken.getAuthorizedClientRegistrationId(),
                    authentication,
                    request);

            if (client != null && client.getRefreshToken() != null) {
                userService.updateSocialRefreshToken(userId, client.getRefreshToken().getTokenValue());
            }
        }

        Token jwtToken = jwtProvider.issueToken(userId);

        String redirectUrl = UriComponentsBuilder.fromUriString(frontendProperties.baseUrl())
                .path(frontendProperties.mainPage())
                .queryParam("accessToken", jwtToken.accessToken())
                .queryParam("refreshToken", jwtToken.refreshToken())
                .build()
                .toUriString();

        response.sendRedirect(redirectUrl);
    }
}