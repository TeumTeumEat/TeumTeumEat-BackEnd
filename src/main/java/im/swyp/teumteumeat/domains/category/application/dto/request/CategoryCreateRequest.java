package im.swyp.teumteumeat.domains.category.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CategoryCreateRequest(

        @NotBlank(message = "이름은 비어있을 수 없습니다.")
        String name,

        @NotBlank(message = "분류 경로는 비어있을 수 없습니다.")
        String path,

        String description
) {
}
