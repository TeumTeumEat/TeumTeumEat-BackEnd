package im.swyp.teumteumeat.domains.category.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record CategoryResponse(

        @Schema(description = "카테고리 ID", example = "1")
        Long categoryId,

        @Schema(description = "카테고리명", example = "정보처리기사 필기 1단원")
        String name,

        @Schema(description = "분류", example = "/IT/정보처리기사/필기")
        String path,

        @Schema(description = "설명", example = "(Nullable) 설명입니다.")
        String description
) {
}
