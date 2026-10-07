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

    /**
     * 직전 주차 (지난주 리그 결과 조회용)
     * 이미 끝난 주이므로 '오늘' 시작을 주 종료 시각으로 두어 오늘 스낵은 0으로 집계된다.
     */
    public LeagueWeek previous() {
        return new LeagueWeek(weekStart.minusWeeks(1), weekStart, weekStart, now);
    }

    /**
     * 동점자 정렬용 스트릭 기준일
     * 진행 중인 주는 오늘, 끝난 주는 마지막 날(일요일) 기준으로 계산해 결과가 바뀌지 않도록 한다.
     */
    public LocalDate streakReferenceDate() {
        LocalDate lastDate = nextWeekStart.toLocalDate().minusDays(1);
        LocalDate today = now.toLocalDate();
        return today.isBefore(lastDate) ? today : lastDate;
    }

    /**
     * 랭킹 캐시 키 (주 시작일:스트릭 기준일)
     * 진행 중인 주는 날짜가 바뀌면 키도 바뀌어, 자정 이후 오늘 스낵 수/스트릭이 새로 계산된다.
     */
    public String cacheKey() {
        return weekStartDate() + ":" + streakReferenceDate();
    }

    public LocalDate weekStartDate() {
        return weekStart.toLocalDate();
    }

    public long remainingSeconds() {
        return Duration.between(now, nextWeekStart).getSeconds();
    }
}
