package im.swyp.teumteumeat.domains.notification.presentation.scheduler;

import im.swyp.teumteumeat.domains.notification.application.usecase.NotificationUseCase;
import im.swyp.teumteumeat.global.component.DistributedLockFacade;
import im.swyp.teumteumeat.global.exception.BaseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private static final String SENT_KEY_PREFIX = "notification:commute:";
    // 블루-그린 배포 중 두 인스턴스가 동시에 떠 있어도 분 단위로 한 번만 발송되도록 하는 키 유지 시간
    private static final long SENT_KEY_TTL_SECONDS = Duration.ofMinutes(5).toSeconds();

    private final NotificationUseCase notificationUseCase;
    private final DistributedLockFacade distributedLockFacade;

    // 매일 1분마다 대상 유저에게 푸쉬 알림 전송
    @Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")
    public void schedule() {
        LocalDateTime minuteStart = LocalDateTime.now().withSecond(0).withNano(0);
        if (!markSent(SENT_KEY_PREFIX + minuteStart)) {
            return;
        }

        LocalTime now = minuteStart.toLocalTime();
        LocalTime minuteEnd = now.plusSeconds(59);

        notificationUseCase.sendNotifications(now, minuteEnd);
    }

    private boolean markSent(String key) {
        try {
            distributedLockFacade.checkAndSetCooldown(key, SENT_KEY_TTL_SECONDS);
            return true;
        } catch (BaseException e) {
            log.debug("Commute notification already sent by another instance: {}", key);
            return false;
        }
    }
}
