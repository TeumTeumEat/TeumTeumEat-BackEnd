package im.swyp.teumteumeat.global.security.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import im.swyp.teumteumeat.global.common.ApiResponse;
import im.swyp.teumteumeat.global.security.constant.AuthResponseCode;
import im.swyp.teumteumeat.global.security.properties.OAuth2RedirectProperties;
import im.swyp.teumteumeat.global.security.repository.HttpCookieOAuth2AuthorizationRequestRepository;
import im.swyp.teumteumeat.global.utils.CookieUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2ResponseHandler {

    private final CookieUtils cookieUtils;
    private final ObjectMapper objectMapper;
    private final OAuth2RedirectProperties oAuth2RedirectProperties;

    public void sendRedirectOrJson(HttpServletRequest request, HttpServletResponse response,
                                   Map<String, String> queryParams, int statusOnJson, Object jsonBody) throws IOException {
        String redirectUri = cookieUtils
                .getCookie(request, HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME)
                .map(Cookie::getValue)
                .orElse(null);

        if (redirectUri != null) {
            if (!isAllowedRedirectUri(redirectUri)) {
                log.warn("허용되지 않은 redirect_uri 시도: {}", redirectUri);
                writeJson(response, HttpServletResponse.SC_BAD_REQUEST,
                        ApiResponse.ofFail(AuthResponseCode.INVALID_REDIRECT_URI));
                return;
            }
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(redirectUri);
            queryParams.forEach(builder::queryParam);
            response.sendRedirect(builder.build().toUriString());
        } else {
            writeJson(response, statusOnJson, jsonBody);
        }
    }

    private boolean isAllowedRedirectUri(String redirectUri) {
        URI uri = URI.create(redirectUri);
        String origin = uri.getScheme() + "://" + uri.getHost()
                + (uri.getPort() == -1 ? "" : ":" + uri.getPort());
        return oAuth2RedirectProperties.allowedRedirectUris().stream()
                .anyMatch(allowed -> allowed.equalsIgnoreCase(origin));
    }

    private void writeJson(HttpServletResponse response, int status, Object body) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setStatus(status);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
