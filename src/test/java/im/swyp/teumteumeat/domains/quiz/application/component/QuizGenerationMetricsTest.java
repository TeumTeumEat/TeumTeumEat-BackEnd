package im.swyp.teumteumeat.domains.quiz.application.component;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class QuizGenerationMetricsTest {

    private final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
    private final QuizGenerationMetrics metrics = new QuizGenerationMetrics(meterRegistry);

    @Test
    void 미터는_생성_시점에_등록되어_값이_쌓이기_전에도_조회된다() {
        assertThat(meterRegistry.find("quiz.generation.duration").timer()).isNotNull();
        assertThat(meterRegistry.find("quiz.generation.rounds").summary()).isNotNull();
        assertThat(meterRegistry.find("quiz.validation.checked").counter()).isNotNull();
        assertThat(meterRegistry.find("quiz.validation.invalid").counter()).isNotNull();
        assertThat(meterRegistry.find("quiz.validation.skipped").counter()).isNotNull();
    }

    @Test
    void 생성_소요시간과_라운드_수를_기록한다() {
        metrics.recordGeneration(Duration.ofMillis(1500), 2);
        metrics.recordGeneration(Duration.ofMillis(500), 1);

        var timer = meterRegistry.get("quiz.generation.duration").timer();
        assertThat(timer.count()).isEqualTo(2);
        assertThat(timer.totalTime(TimeUnit.MILLISECONDS)).isEqualTo(2000.0);

        var rounds = meterRegistry.get("quiz.generation.rounds").summary();
        assertThat(rounds.count()).isEqualTo(2);
        assertThat(rounds.totalAmount()).isEqualTo(3.0);
    }

    @Test
    void 검증_카운터를_퀴즈_수만큼_누적한다() {
        metrics.recordValidationChecked(10);
        metrics.recordValidationChecked(5);
        metrics.recordValidationInvalid(3);
        metrics.recordValidationSkipped(4);

        assertThat(meterRegistry.get("quiz.validation.checked").counter().count()).isEqualTo(15.0);
        assertThat(meterRegistry.get("quiz.validation.invalid").counter().count()).isEqualTo(3.0);
        assertThat(meterRegistry.get("quiz.validation.skipped").counter().count()).isEqualTo(4.0);
    }
}
