package im.swyp.teumteumeat.global.security.client.google;

import im.swyp.teumteumeat.global.security.dto.GoogleTokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.http.MediaType;

@Component
@RequiredArgsConstructor
public class GoogleAuthClient {

    private final RestClient restClient = RestClient.create();

    public GoogleTokenResponse getToken(String clientId, String clientSecret, String code, String grantType,
            String redirectUri) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("code", code);
        formData.add("grant_type", grantType);
        formData.add("redirect_uri", redirectUri);

        return restClient.post()
                .uri("https://oauth2.googleapis.com/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formData)
                .retrieve()
                .body(GoogleTokenResponse.class);
    }
}
