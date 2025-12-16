package im.swyp.teumteumeat.global.security.component;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class CustomOAuth2AuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private final OAuth2AuthorizationRequestResolver defaultResolver;

    public CustomOAuth2AuthorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository) {
        this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository, "/oauth2/authorization");
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request);
        return customizeAuthorizationRequest(authorizationRequest);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request, clientRegistrationId);
        return customizeAuthorizationRequest(authorizationRequest);
    }

    private OAuth2AuthorizationRequest customizeAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest) {
        if (authorizationRequest == null) {
            return null;
        }

        // Apple 로그인인 경우에만 커스텀 로직 적용
        if ("apple".equalsIgnoreCase(authorizationRequest.getAttribute("registration_id"))) {
            Map<String, Object> additionalParameters = new LinkedHashMap<>(
                    authorizationRequest.getAdditionalParameters());

            // 1. response_mode=form_post 추가
            additionalParameters.put("response_mode", "form_post");

            // 2. nonce 추가 (Apple 요구사항)
            String nonce = UUID.randomUUID().toString();
            additionalParameters.put("nonce", nonce);

            // 3. redirect_uri를 https로 강제 변환
            String redirectUri = authorizationRequest.getRedirectUri();
            if (redirectUri != null && redirectUri.startsWith("http://")) {
                redirectUri = redirectUri.replaceFirst("http://", "https://");
            }

            return OAuth2AuthorizationRequest.from(authorizationRequest)
                    .additionalParameters(additionalParameters)
                    .attributes(attrs -> {
                        attrs.put("nonce", nonce);
                        // attributes에도 response_mode 추가 (혹시 모를 상황 대비)
                        attrs.put("response_mode", "form_post");
                    })
                    .redirectUri(redirectUri)
                    .build();
        }

        return authorizationRequest;
    }
}
