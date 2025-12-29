package im.swyp.teumteumeat.domains.document.application.dto.request;

public record OcrPartRequest(
    String fileKey,
    String ocrText,
    Integer partIndex
) {
}