package im.swyp.teumteumeat.domains.notification.presentation.scheduler;

import groovy.util.logging.Slf4j;
import im.swyp.teumteumeat.domains.notification.application.usecase.NotificationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final NotificationUseCase notificationUseCase;

    // 평일(월-금)에만 1분마다 대상 유저에게 푸쉬 알림 전송
    @Scheduled(cron = "0 * * * * *")
    public void schedule() {
        LocalTime now = LocalTime.now().withSecond(0).withNano(0);
        LocalTime minuteEnd = now.plusSeconds(59);

        notificationUseCase.sendNotifications(now, minuteEnd);
    }
}
