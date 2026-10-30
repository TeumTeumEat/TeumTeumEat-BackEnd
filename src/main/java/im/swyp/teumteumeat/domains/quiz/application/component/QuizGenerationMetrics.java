package im.swyp.teumteumeat.domains.quiz.application.component;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.time.Duration;

// 퀴즈 생성/검증 메트릭. 미터는 생성 시점에 한 번만 등록하고 재사용한다.
@Component
public class QuizGenerationMetrics {

    private final Timer generationDuration;
    private final DistributionSummary generationRounds;
    private final Counter validationChecked;
    private final Counter validationInvalid;
    private final Counter validationSkipped;

    public QuizGenerationMetrics(MeterRegistry meterRegistry) {
        this.generationDuration = Timer.builder("quiz.generation.duration")
                .description("퀴즈 세트 생성(사전 검증 및 부족분 재시도 포함) 소요시간")
                .publishPercentileHistogram()
                .register(meterRegistry);
        this.generationRounds = DistributionSummary.builder("quiz.generation.rounds")
                .description("퀴즈 세트 하나를 확보하는 데 걸린 생성 라운드 수")
                .register(meterRegistry);
        this.validationChecked = Counter.builder("quiz.validation.checked")
                .description("검증 콜에 포함된 퀴즈 수")
                .register(meterRegistry);
        this.validationInvalid = Counter.builder("quiz.validation.invalid")
                .description("검증에서 무효 판정되어 제외된 퀴즈 수")
                .register(meterRegistry);
        this.validationSkipped = Counter.builder("quiz.validation.skipped")
                .description("검증 콜 실패로 검증 없이 통과된 퀴즈 수")
                .register(meterRegistry);
    }

    public void recordGeneration(Duration elapsed, int rounds) {
        generationDuration.record(elapsed);
        generationRounds.record(rounds);
    }

    public void recordValidationChecked(int quizCount) {
        validationChecked.increment(quizCount);
    }

    public void recordValidationInvalid(int quizCount) {
        validationInvalid.increment(quizCount);
    }

    public void recordValidationSkipped(int quizCount) {
        validationSkipped.increment(quizCount);
    }
}
