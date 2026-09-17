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
        if (exception instanceof OAuth2AuthenticationException oauth2Exception) {
            OAuth2Error error = oauth2Exception.getError();
            log.warn("OAuth2 Login Failed. Request URI: {}, Error Code: {}, Description: {}",
                    request.getRequestURI(), error.getErrorCode(), error.getDescription());
        } else {
            log.warn("OAuth2 Login Failed. Request URI: {}, Message: {}",
                    request.getRequestURI(), exception.getMessage());
        }

        oAuth2ResponseHandler.sendRedirectOrJson(
                request, response,
                Map.of("error", "oauth2_login_failed"),
                null,
                HttpServletResponse.SC_UNAUTHORIZED,
                ApiResponse.ofFail(AuthResponseCode.UNAUTHORIZED)
        );
    }
}
