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
}
