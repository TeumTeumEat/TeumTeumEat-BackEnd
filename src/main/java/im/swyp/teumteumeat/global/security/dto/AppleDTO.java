package im.swyp.teumteumeat.global.security.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AppleDTO {

    @Data
    @NoArgsConstructor
    public static class AppleTokenResponse {
        private String access_token;
        private String token_type;
        private Long expires_in;
        private String refresh_token;
        private String id_token;
    }
}
