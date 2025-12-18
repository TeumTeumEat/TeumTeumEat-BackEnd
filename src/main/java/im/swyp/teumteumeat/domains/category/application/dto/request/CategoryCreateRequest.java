package im.swyp.teumteumeat.domains.category.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record CategoryCreateRequest(

        @NotBlank(message = "이름은 비어있을 수 없습니다.")
        @Schema(description = "카테고리명", example = "정보처리기사 필기 1단원")
        String name,

        @NotBlank(message = "분류 경로는 비어있을 수 없습니다.")
        @Schema(description = "분류", example = "/IT/정보처리기사/필기")
        String path,

        @Schema(description = "설명", example = "(Nullable) 설명입니다.")
        String description
) {
}
