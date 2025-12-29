package im.swyp.teumteumeat.domains.document.application.dto.request;

public record OcrInitRequest(
    String fileKey,
    Integer totalParts,
    Boolean needOcr,
    String rawContent
) {
}