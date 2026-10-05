package im.swyp.teumteumeat.domains.league.domain.vo;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class LeagueWeekTest {

    @Test
    void 주중에는_이번_주_월요일부터_다음_주_월요일_전까지가_집계_범위다() {
        // 2026-10-08 (목)
        LeagueWeek week = LeagueWeek.of(LocalDateTime.of(2026, 10, 8, 14, 30));

        assertThat(week.weekStart()).isEqualTo(LocalDateTime.of(2026, 10, 5, 0, 0));
        assertThat(week.nextWeekStart()).isEqualTo(LocalDateTime.of(2026, 10, 12, 0, 0));
        assertThat(week.todayStart()).isEqualTo(LocalDateTime.of(2026, 10, 8, 0, 0));
        assertThat(week.weekStartDate()).isEqualTo(LocalDate.of(2026, 10, 5));
    }

    @Test
    void 일요일_23시59분59초는_아직_이번_주에_속한다() {
        LeagueWeek week = LeagueWeek.of(LocalDateTime.of(2026, 10, 11, 23, 59, 59));

        assertThat(week.weekStart()).isEqualTo(LocalDateTime.of(2026, 10, 5, 0, 0));
        assertThat(week.remainingSeconds()).isEqualTo(1);
    }

    @Test
    void 월요일_00시가_되면_새로운_주가_시작된다() {
        LeagueWeek week = LeagueWeek.of(LocalDateTime.of(2026, 10, 12, 0, 0));

        assertThat(week.weekStart()).isEqualTo(LocalDateTime.of(2026, 10, 12, 0, 0));
        assertThat(week.nextWeekStart()).isEqualTo(LocalDateTime.of(2026, 10, 19, 0, 0));
        assertThat(week.remainingSeconds()).isEqualTo(7 * 24 * 60 * 60);
    }

    @Test
    void 직전_주차는_지난주_월요일부터_이번_주_월요일_전까지다() {
        LeagueWeek lastWeek = LeagueWeek.of(LocalDateTime.of(2026, 10, 8, 14, 30)).previous();

        assertThat(lastWeek.weekStart()).isEqualTo(LocalDateTime.of(2026, 9, 28, 0, 0));
        assertThat(lastWeek.nextWeekStart()).isEqualTo(LocalDateTime.of(2026, 10, 5, 0, 0));
    }

    @Test
    void 진행_중인_주의_스트릭_기준일은_오늘이다() {
        LeagueWeek week = LeagueWeek.of(LocalDateTime.of(2026, 10, 8, 14, 30));

        assertThat(week.streakReferenceDate()).isEqualTo(LocalDate.of(2026, 10, 8));
    }

    @Test
    void 끝난_주의_스트릭_기준일은_그_주_일요일이다() {
        LeagueWeek lastWeek = LeagueWeek.of(LocalDateTime.of(2026, 10, 8, 14, 30)).previous();

        assertThat(lastWeek.streakReferenceDate()).isEqualTo(LocalDate.of(2026, 10, 4));
    }
}
