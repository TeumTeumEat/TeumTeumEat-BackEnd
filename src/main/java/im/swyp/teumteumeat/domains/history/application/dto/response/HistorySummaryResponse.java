package im.swyp.teumteumeat.domains.history.application.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record HistorySummaryResponse(
        String title,
        String summary, // 전체 요약
        LocalDateTime createdAt) {
}
