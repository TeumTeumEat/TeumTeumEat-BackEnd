package im.swyp.teumteumeat.domains.league.application.component;

import im.swyp.teumteumeat.global.exception.BaseException;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class LeagueTimeResolverTest {

    private static final long ONE_HOUR = 60 * 60;

    @Test
    void 활성화되어_있으면_오프셋만큼_옮긴_시각을_반환한다() {
        LeagueTimeResolver resolver = new LeagueTimeResolver(true);

        LocalDateTime resolved = resolver.now(ONE_HOUR);

        assertThat(resolved).isCloseTo(LocalDateTime.now().plusSeconds(ONE_HOUR), within(Duration.ofSeconds(1).toMillis(), java.time.temporal.ChronoUnit.MILLIS));
    }

    @Test
    void 음수_오프셋이면_과거_시각을_반환한다() {
        LeagueTimeResolver resolver = new LeagueTimeResolver(true);

        LocalDateTime resolved = resolver.now(-ONE_HOUR);

        assertThat(resolved).isCloseTo(LocalDateTime.now().minusSeconds(ONE_HOUR), within(Duration.ofSeconds(1).toMillis(), java.time.temporal.ChronoUnit.MILLIS));
    }

    @Test
    void 헤더가_없으면_실제_시각을_반환한다() {
        LeagueTimeResolver resolver = new LeagueTimeResolver(true);

        assertThat(resolver.now(null)).isCloseTo(LocalDateTime.now(), within(1000L, java.time.temporal.ChronoUnit.MILLIS));
    }

    @Test
    void 비활성화되어_있으면_오프셋을_무시한다() {
        LeagueTimeResolver resolver = new LeagueTimeResolver(false);

        assertThat(resolver.now(ONE_HOUR)).isCloseTo(LocalDateTime.now(), within(1000L, java.time.temporal.ChronoUnit.MILLIS));
    }

    @Test
    void 비활성화되어_있으면_범위를_벗어난_오프셋도_무시한다() {
        LeagueTimeResolver resolver = new LeagueTimeResolver(false);

        assertThat(resolver.now(Long.MAX_VALUE)).isCloseTo(LocalDateTime.now(), within(1000L, java.time.temporal.ChronoUnit.MILLIS));
    }

    @Test
    void 허용_범위를_벗어난_오프셋은_예외를_던진다() {
        LeagueTimeResolver resolver = new LeagueTimeResolver(true);
        long tooFar = Duration.ofDays(367).getSeconds();

        assertThatThrownBy(() -> resolver.now(tooFar)).isInstanceOf(BaseException.class);
        assertThatThrownBy(() -> resolver.now(-tooFar)).isInstanceOf(BaseException.class);
        assertThatThrownBy(() -> resolver.now(Long.MIN_VALUE)).isInstanceOf(BaseException.class);
    }

    @Test
    void 활성화되어_있고_헤더가_있을_때만_오프셋_요청이다() {
        assertThat(new LeagueTimeResolver(true).isSimulated(ONE_HOUR)).isTrue();
        assertThat(new LeagueTimeResolver(true).isSimulated(0L)).isTrue();
        assertThat(new LeagueTimeResolver(true).isSimulated(null)).isFalse();
        assertThat(new LeagueTimeResolver(false).isSimulated(ONE_HOUR)).isFalse();
    }
}
