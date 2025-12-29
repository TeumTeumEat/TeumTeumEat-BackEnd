package im.swyp.teumteumeat.global.security.client.apple;

import im.swyp.teumteumeat.global.security.dto.AppleTokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.http.MediaType;

@Component
@RequiredArgsConstructor
public class AppleAuthClient {

    private final RestClient restClient = RestClient.create();

    public AppleTokenResponse getToken(String clientId, String clientSecret, String code, String grantType,
            String redirectUri) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("code", code);
        formData.add("grant_type", grantType);
        formData.add("redirect_uri", redirectUri);

        return restClient.post()
                .uri("https://appleid.apple.com/auth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formData)
                .retrieve()
                .body(AppleTokenResponse.class);
    }
}
