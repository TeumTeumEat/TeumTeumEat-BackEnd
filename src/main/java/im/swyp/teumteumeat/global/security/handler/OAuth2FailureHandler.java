package im.swyp.teumteumeat.global.security.handler;

import im.swyp.teumteumeat.global.config.properties.FrontendProperties;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class OAuth2FailureHandler extends SimpleUrlAuthenticationFailureHandler {

    // private final MobileAppProperties mobileAppProperties; //todo 모바일 앱은 DeepLink
    // 생성
    private final FrontendProperties frontendProperties;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException {
        log.error("OAuth2 Login Failed. Request URI: {}", request.getRequestURI());
        log.error("Error Message: {}", exception.getMessage());

        if (exception instanceof org.springframework.security.oauth2.core.OAuth2AuthenticationException oauth2Exception) {
            org.springframework.security.oauth2.core.OAuth2Error error = oauth2Exception.getError();
            log.error("OAuth2 Error Code: {}", error.getErrorCode());
            log.error("OAuth2 Error Description: {}", error.getDescription());
            log.error("OAuth2 Error URI: {}", error.getUri());
        }

        log.error("Stack Trace: ", exception);
        response.sendRedirect(frontendProperties.baseUrl() + frontendProperties.loginFailPage());
    }
}