package im.swyp.teumteumeat.global.security.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record AuthTokenResponse(
        @JsonProperty("access_token") String accessToken,

        @JsonProperty("refresh_token") String refreshToken) {
}
