package im.swyp.teumteumeat.domains.document.application.dto.request;

import im.swyp.teumteumeat.domains.category.domain.constant.DocumentErrorType;

public record OcrPartRequest(
    String fileKey,
    String ocrText,
    Integer partIndex,
    Boolean success,
    DocumentErrorType reason
) {
}