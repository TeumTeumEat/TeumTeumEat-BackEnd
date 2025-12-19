package im.swyp.teumteumeat.domains.document.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record DocumentCreateRequest(

        @Schema(description = "파일명", example = "1강.pdf")
        String fileName,

        @Schema(description = "파일 Key", example = "z121dfs_1강.pdf")
        String fileKey
) {
}
