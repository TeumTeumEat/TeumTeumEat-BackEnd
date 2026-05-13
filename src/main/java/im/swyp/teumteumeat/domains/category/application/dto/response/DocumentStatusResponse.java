package im.swyp.teumteumeat.domains.category.application.dto.response;

import im.swyp.teumteumeat.domains.category.domain.constant.DocumentErrorType;
import im.swyp.teumteumeat.domains.document.domain.constant.FileStatus;
import im.swyp.teumteumeat.domains.document.persistence.entity.Document;
import im.swyp.teumteumeat.global.sse.dto.SseResponse;

import java.time.Duration;
import java.time.LocalDateTime;

import static im.swyp.teumteumeat.domains.document.domain.constant.FileStatus.*;

public record DocumentStatusResponse(
        FileStatus status,
        Long remain,
        DocumentErrorType reason
) implements SseResponse {

    public static DocumentStatusResponse from(Document document) {
        return switch (document.getStatus()) {
            case PENDING -> pending();
            case PROCESSING ->
                    processing(Math.max(0, Duration.between(LocalDateTime.now(), document.getEstimateTime()).toMillis()));
            case COMPLETED -> completed();
            case FAILED -> failed(document.getErrorReason());
        };
    }

    private static DocumentStatusResponse pending() {
        return new DocumentStatusResponse(PENDING, null, null);
    }

    private static DocumentStatusResponse processing(Long ms) {
        return new DocumentStatusResponse(PROCESSING, ms, null);
    }

    private static DocumentStatusResponse completed() {
        return new DocumentStatusResponse(COMPLETED, null, null);
    }

    private static DocumentStatusResponse failed(DocumentErrorType reason) {
        return new DocumentStatusResponse(FAILED, null, reason);
    }

    @Override
    public String getStatus() {
        return status.name();
    }
}