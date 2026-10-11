package im.swyp.teumteumeat.domains.league.application.component;

import im.swyp.teumteumeat.domains.league.domain.constant.LeagueResponseCode;
import im.swyp.teumteumeat.global.exception.BaseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 리그 조회 API의 기준 시각 결정
 * dev/local 환경에서만 요청 헤더(X-League-Time-Offset)로 받은 오프셋(초)을 현재 시각에 더해,
 * 리셋 직전/직후 화면을 테스트할 수 있게 한다.
 * - 헤더가 붙은 그 요청의 조회 계산에만 적용되며 상태를 저장하지 않는다.
 * - 스낵 적립, 푸시 알림 등 다른 기능은 항상 실제 시각을 사용한다.
 * - 운영(prod)에서는 비활성화되어 헤더를 무시한다.
 */
@Component
public class LeagueTimeResolver {

    public static final String TIME_OFFSET_HEADER = "X-League-Time-Offset";

    // 오프셋 상한 (지난주 결과 조회 등을 고려해 앞뒤 1년)
    private static final long MAX_OFFSET_SECONDS = Duration.ofDays(366).getSeconds();

    private final boolean timeOffsetEnabled;

    public LeagueTimeResolver(@Value("${league.time-offset.enabled:false}") boolean timeOffsetEnabled) {
        this.timeOffsetEnabled = timeOffsetEnabled;
    }

    public LocalDateTime now(Long timeOffsetSeconds) {
        LocalDateTime now = LocalDateTime.now();
        if (!timeOffsetEnabled || timeOffsetSeconds == null) {
            return now;
        }
        // Math.abs는 Long.MIN_VALUE에서 오버플로되므로 양쪽 경계를 직접 비교
        if (timeOffsetSeconds > MAX_OFFSET_SECONDS || timeOffsetSeconds < -MAX_OFFSET_SECONDS) {
            throw new BaseException(LeagueResponseCode.INVALID_TIME_OFFSET);
        }
        return now.plusSeconds(timeOffsetSeconds);
    }
}
