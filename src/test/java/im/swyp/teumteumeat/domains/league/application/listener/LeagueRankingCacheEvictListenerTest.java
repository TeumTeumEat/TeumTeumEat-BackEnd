package im.swyp.teumteumeat.domains.league.application.listener;

import im.swyp.teumteumeat.domains.league.application.component.LeagueRankingProvider;
import im.swyp.teumteumeat.domains.league.domain.event.SnackEarnedEvent;
import im.swyp.teumteumeat.domains.league.domain.vo.LeagueWeek;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class LeagueRankingCacheEvictListenerTest {

    private final LeagueRankingProvider leagueRankingProvider = mock(LeagueRankingProvider.class);
    private final LeagueRankingCacheEvictListener listener = new LeagueRankingCacheEvictListener(leagueRankingProvider);

    @Test
    void 적립_시각이_속한_주차의_캐시를_삭제한다() {
        LocalDateTime earnedAt = LocalDateTime.of(2026, 10, 11, 23, 59, 59);

        listener.handleSnackEarnedEvent(new SnackEarnedEvent(earnedAt));

        verify(leagueRankingProvider).evict(LeagueWeek.of(earnedAt));
    }

    @Test
    void 캐시_삭제에_실패해도_예외를_던지지_않는다() {
        doThrow(new RuntimeException("redis down")).when(leagueRankingProvider).evict(any());

        assertThatCode(() -> listener.handleSnackEarnedEvent(new SnackEarnedEvent(LocalDateTime.now())))
                .doesNotThrowAnyException();
    }
}
