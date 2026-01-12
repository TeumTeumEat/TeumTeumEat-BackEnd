package im.swyp.teumteumeat.domains.document.application.dto.response;

import im.swyp.teumteumeat.domains.document.domain.constant.FileStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record DocumentResponse(

        @Schema(description = "문서 ID", example = "1")
        Long documentId,

        @Schema(description = "파일명", example = "1강.pdf")
        String fileName,

        @Schema(description = "파일 Key", example = "1/sfwsw_1강.pdf")
        String fileKey,

        @Schema(description = "OCR 처리 상태", example = "PENDING (Schema ENUM 참조)")
        FileStatus status,

        @Schema(description = "파일 처리 예상 소요 시간(status=PROCESSING일때만 반환)", example = "1")
        Integer estimateTime
) {
}
