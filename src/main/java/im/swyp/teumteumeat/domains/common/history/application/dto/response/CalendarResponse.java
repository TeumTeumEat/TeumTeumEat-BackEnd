package im.swyp.teumteumeat.domains.common.history.application.dto.response;

import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record CalendarResponse(
        List<LocalDate> stampedDates,
        int totalStamps, // 전체 누적 스탬프
        int monthlyStamps, // 이번 달 스탬프
        int currentStreak) {
}
