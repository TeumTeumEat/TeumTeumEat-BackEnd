package im.swyp.teumteumeat.global.security.handler;

import im.swyp.teumteumeat.domains.user.domain.constant.Role;
import im.swyp.teumteumeat.domains.user.domain.service.UserService;
import im.swyp.teumteumeat.global.security.component.OAuth2ResponseHandler;
import im.swyp.teumteumeat.global.security.dto.CustomUserDetails;
import im.swyp.teumteumeat.global.security.token.JwtProvider;
import im.swyp.teumteumeat.global.security.token.Token;
import im.swyp.teumteumeat.global.security.token.TokenResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final OAuth2AuthorizedClientRepository oAuth2AuthorizedClientRepository;
    private final UserService userService;
    private final OAuth2ResponseHandler oAuth2ResponseHandler;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException {
        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        Long userId = principal.getUserId();
        Role role = principal.role();

        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            OAuth2AuthorizedClient client = oAuth2AuthorizedClientRepository.loadAuthorizedClient(
                    oauthToken.getAuthorizedClientRegistrationId(),
                    authentication,
                    request);

            if (client != null && client.getRefreshToken() != null) {
                userService.updateSocialRefreshToken(userId, client.getRefreshToken().getTokenValue());
            }
        }

        Token jwtToken = jwtProvider.issueToken(userId, role);
        TokenResponse tokenResponse = TokenResponse.builder()
                .accessToken(jwtToken.accessToken())
                .refreshToken(jwtToken.refreshToken())
                .build();

        oAuth2ResponseHandler.sendRedirectOrJson(
                request, response,
                Map.of("accessToken", jwtToken.accessToken(), "refreshToken", jwtToken.refreshToken()),
                HttpStatus.OK.value(),
                tokenResponse
        );
    }
}
