package im.swyp.teumteumeat.domains.user.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public record NameRequest(

        @NotBlank(message = "비어있을 수 없습니다.")
        String name
) {
}
