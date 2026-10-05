package im.swyp.teumteumeat.domains.league.domain.vo;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

/**
 * 리그 집계 기준 주차 (월 00:00 ~ 다음 주 월 00:00, 서버 시간대 KST 기준)
 * 스낵 적립 시각(created_date)과 같은 기준으로 비교하기 위해 LocalDateTime을 사용한다.
 *
 * @param weekStart     이번 주 시작 (월요일 00:00, 포함)
 * @param nextWeekStart 다음 주 시작 (다음 월요일 00:00, 미포함) = 리그 리셋 시각
 * @param todayStart    오늘 시작 (00:00, 포함)
 * @param now           기준 시각
 */
public record LeagueWeek(
        LocalDateTime weekStart,
        LocalDateTime nextWeekStart,
        LocalDateTime todayStart,
        LocalDateTime now
) {

    public static LeagueWeek of(LocalDateTime now) {
        LocalDate today = now.toLocalDate();
        LocalDateTime weekStart = today
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .atStartOfDay();

        return new LeagueWeek(weekStart, weekStart.plusWeeks(1), today.atStartOfDay(), now);
    }

    public LocalDate weekStartDate() {
        return weekStart.toLocalDate();
    }

    public long remainingSeconds() {
        return Duration.between(now, nextWeekStart).getSeconds();
    }
}
