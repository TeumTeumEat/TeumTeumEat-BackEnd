package im.swyp.teumteumeat.domains.user.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record NameRequest(

        @NotBlank(message = "비어있을 수 없습니다.")
        @Size(min = 1, max = 10, message = "10자 이하이어야 합니다.")
        @Pattern(regexp = "^[a-zA-Z0-9가-힣]*$", message = "공백없는 한영숫자로만 가능합니다.")
        @Schema(description = "이름", example = "이효재")
        String name
) {
}
