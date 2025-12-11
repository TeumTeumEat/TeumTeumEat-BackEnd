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
public class OAuth2FailureHandler extends SimpleUrlAuthenticationFailureHandler {

    // private final MobileAppProperties mobileAppProperties; //todo 모바일 앱은 DeepLink 생성
    private final FrontendProperties frontendProperties;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        response.sendRedirect(frontendProperties.baseUrl() + frontendProperties.loginFailPage());
    }
}