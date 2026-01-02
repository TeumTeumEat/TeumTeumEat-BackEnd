package im.swyp.teumteumeat.domains.document.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record DocumentIdResponse(
        @Schema(description = "문서 ID", example = "1") Long documentId) {
}
