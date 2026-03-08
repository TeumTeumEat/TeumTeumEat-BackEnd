package im.swyp.teumteumeat.domains.document.application.dto.request;

import im.swyp.teumteumeat.domains.category.application.dto.response.DocumentErrorType;

public record OcrInitRequest(
    String fileName,
    String fileKey,
    Integer totalParts,
    Boolean needOcr,
    String rawContent,
    Integer estimateTime,
    Boolean success,
    DocumentErrorType reason
) {
}