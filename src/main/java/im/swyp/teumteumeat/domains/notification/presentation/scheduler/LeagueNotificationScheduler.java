package im.swyp.teumteumeat.domains.notification.presentation.scheduler;

import im.swyp.teumteumeat.domains.league.domain.vo.LeagueWeek;
import im.swyp.teumteumeat.domains.notification.application.usecase.LeagueNotificationUseCase;
import im.swyp.teumteumeat.global.component.DistributedLockFacade;
import im.swyp.teumteumeat.global.exception.BaseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Component
// 리그 알림 발송 여부를 환경변수(LEAGUE_NOTIFICATION_SCHEDULER_ENABLED)로 제어한다. 값이 없으면 활성화
@ConditionalOnProperty(name = "notification.league.scheduler-enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class LeagueNotificationScheduler {

    private static final String DEADLINE_KEY_PREFIX = "league:notification:deadline:";
    private static final String RESULT_KEY_PREFIX = "league:notification:result:";
    // 블루-그린 배포 중 두 인스턴스가 동시에 떠 있어도 주차별로 한 번만 발송되도록 하는 키 유지 시간
    private static final long SENT_KEY_TTL_SECONDS = Duration.ofDays(1).toSeconds();

    private final LeagueNotificationUseCase leagueNotificationUseCase;
    private final DistributedLockFacade distributedLockFacade;

    // 일요일 20:00, 리그 마감(월 00:00) 4시간 전 참여 독려 알림
    @Scheduled(cron = "0 0 20 * * SUN", zone = "Asia/Seoul")
    public void scheduleDeadline() {
        LocalDateTime now = LocalDateTime.now();
        String key = DEADLINE_KEY_PREFIX + LeagueWeek.of(now).weekStartDate();
        if (!markSent(key)) {
            return;
        }
        leagueNotificationUseCase.sendDeadlineNotifications(now);
    }

    // 월요일 08:00, 지난주 리그 결과 확인 알림
    @Scheduled(cron = "0 0 8 * * MON", zone = "Asia/Seoul")
    public void scheduleResult() {
        LocalDateTime now = LocalDateTime.now();
        String key = RESULT_KEY_PREFIX + LeagueWeek.of(now).previous().weekStartDate();
        if (!markSent(key)) {
            return;
        }
        leagueNotificationUseCase.sendResultNotifications(now);
    }

    private boolean markSent(String key) {
        try {
            distributedLockFacade.checkAndSetCooldown(key, SENT_KEY_TTL_SECONDS);
            return true;
        } catch (BaseException e) {
            log.info("League notification already sent by another instance: {}", key);
            return false;
        }
    }
}
