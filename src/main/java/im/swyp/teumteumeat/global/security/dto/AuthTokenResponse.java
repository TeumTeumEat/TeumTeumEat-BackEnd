package im.swyp.teumteumeat.global.security.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record AuthTokenResponse(
        @JsonProperty("accessToken") String accessToken,

        @JsonProperty("refreshToken") String refreshToken) {
}
