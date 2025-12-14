package im.swyp.teumteumeat.global.security.handler;

import im.swyp.teumteumeat.domains.user.persistence.entity.UserEntity;
import im.swyp.teumteumeat.global.config.properties.FrontendProperties;
import im.swyp.teumteumeat.global.security.dto.CustomUserDetails;
import im.swyp.teumteumeat.global.security.token.JwtProvider;
import im.swyp.teumteumeat.global.security.token.Token;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
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
    // private final MobileAppProperties mobileAppProperties; //todo 모바일 앱은 DeepLink
    // 생성
    private final FrontendProperties frontendProperties; // 웹 테스트용

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException {
        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        UserEntity user = principal.user();

        Token jwtToken = jwtProvider.issueToken(user);

        String redirectUrl = UriComponentsBuilder.fromUriString(frontendProperties.baseUrl())
                .path(frontendProperties.mainPage())
                .queryParam("accessToken", jwtToken.accessToken())
                .queryParam("refreshToken", jwtToken.refreshToken())
                .build()
                .toUriString();

        response.sendRedirect(redirectUrl);
    }
}