package im.swyp.teumteumeat.domains.notification.presentation.scheduler;

import im.swyp.teumteumeat.domains.notification.application.usecase.NotificationUseCase;
import im.swyp.teumteumeat.global.common.CommonResponseCode;
import im.swyp.teumteumeat.global.component.DistributedLockFacade;
import im.swyp.teumteumeat.global.exception.BaseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

// 여러 인스턴스가 동시에 스케줄을 실행해도 분 단위 발송 완료 키로 한 번만 발송되는지 확인한다.
class NotificationSchedulerTest {

    private NotificationUseCase notificationUseCase;
    private DistributedLockFacade distributedLockFacade;
    private NotificationScheduler scheduler;

    @BeforeEach
    void setUp() {
        notificationUseCase = mock(NotificationUseCase.class);
        distributedLockFacade = mock(DistributedLockFacade.class);
        scheduler = new NotificationScheduler(notificationUseCase, distributedLockFacade);
    }

    @Test
    void 발송_완료_키를_선점하면_현재_분의_출퇴근_알림을_발송한다() {
        scheduler.schedule();

        verify(distributedLockFacade).checkAndSetCooldown(startsWith("notification:commute:"), anyLong());

        ArgumentCaptor<LocalTime> start = ArgumentCaptor.forClass(LocalTime.class);
        ArgumentCaptor<LocalTime> end = ArgumentCaptor.forClass(LocalTime.class);
        verify(notificationUseCase).sendNotifications(start.capture(), end.capture());
        assertThat(start.getValue().getSecond()).isZero();
        assertThat(end.getValue()).isEqualTo(start.getValue().plusSeconds(59));
    }

    @Test
    void 발송_완료_키는_분_단위로_만든다() {
        scheduler.schedule();

        ArgumentCaptor<String> key = ArgumentCaptor.forClass(String.class);
        verify(distributedLockFacade).checkAndSetCooldown(key.capture(), anyLong());
        assertThat(key.getValue()).matches("notification:commute:\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}");
    }

    @Test
    void 이미_발송된_분이면_출퇴근_알림을_발송하지_않는다() {
        doThrow(new BaseException(CommonResponseCode.TOO_MANY_REQUESTS))
                .when(distributedLockFacade).checkAndSetCooldown(anyString(), anyLong());

        scheduler.schedule();

        verify(notificationUseCase, never()).sendNotifications(any(), any());
    }
}
