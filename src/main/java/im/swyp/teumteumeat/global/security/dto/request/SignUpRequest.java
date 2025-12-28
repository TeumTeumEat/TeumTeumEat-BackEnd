package im.swyp.teumteumeat.global.security.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public class SignUpRequest {

    @Schema(title = "소셜 회원가입 요청 DTO")
    public record Oidc(

            @Schema(description = "OIDC 토큰")
            @NotBlank(message = "OIDC 토큰은 필수 입력값입니다.")
            String idToken,

            boolean termsAgreed
    ) {
    }
}
