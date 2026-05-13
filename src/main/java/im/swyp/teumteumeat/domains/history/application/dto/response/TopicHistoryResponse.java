package im.swyp.teumteumeat.domains.history.application.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record TopicHistoryResponse(
        String categoryName,
        List<DailyHistoryResponse> histories) {
}
