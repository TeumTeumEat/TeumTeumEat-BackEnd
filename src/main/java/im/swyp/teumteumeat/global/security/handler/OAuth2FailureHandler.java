package im.swyp.teumteumeat.global.security.handler;

import im.swyp.teumteumeat.global.common.ApiResponse;
import im.swyp.teumteumeat.global.security.component.OAuth2ResponseHandler;
import im.swyp.teumteumeat.global.security.constant.AuthResponseCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2FailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final OAuth2ResponseHandler oAuth2ResponseHandler;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException {
        log.error("OAuth2 Login Failed. Request URI: {}", request.getRequestURI());
        log.error("Error Message: {}", exception.getMessage());

        if (exception instanceof OAuth2AuthenticationException oauth2Exception) {
            OAuth2Error error = oauth2Exception.getError();
            log.error("OAuth2 Error Code: {}", error.getErrorCode());
            log.error("OAuth2 Error Description: {}", error.getDescription());
            log.error("OAuth2 Error URI: {}", error.getUri());
        }

        log.error("Stack Trace: ", exception);
        oAuth2ResponseHandler.sendRedirectOrJson(
                request, response,
                Map.of("error", "oauth2_login_failed"),
                null,
                HttpServletResponse.SC_UNAUTHORIZED,
                ApiResponse.ofFail(AuthResponseCode.UNAUTHORIZED)
        );
    }
}
