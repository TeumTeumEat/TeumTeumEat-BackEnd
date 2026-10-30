package im.swyp.teumteumeat.domains.notification.presentation.scheduler;

import im.swyp.teumteumeat.domains.notification.application.usecase.LeagueNotificationUseCase;
import im.swyp.teumteumeat.global.common.CommonResponseCode;
import im.swyp.teumteumeat.global.component.DistributedLockFacade;
import im.swyp.teumteumeat.global.exception.BaseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

// 여러 인스턴스가 동시에 스케줄을 실행해도 주차별 발송 완료 키로 한 번만 발송되는지 확인한다.
class LeagueNotificationSchedulerTest {

    private LeagueNotificationUseCase leagueNotificationUseCase;
    private DistributedLockFacade distributedLockFacade;
    private LeagueNotificationScheduler scheduler;

    @BeforeEach
    void setUp() {
        leagueNotificationUseCase = mock(LeagueNotificationUseCase.class);
        distributedLockFacade = mock(DistributedLockFacade.class);
        scheduler = new LeagueNotificationScheduler(leagueNotificationUseCase, distributedLockFacade);
    }

    @Test
    void 발송_완료_키를_선점하면_마감_알림을_발송한다() {
        scheduler.scheduleDeadline();

        verify(distributedLockFacade).checkAndSetCooldown(startsWith("league:notification:deadline:"), anyLong());
        verify(leagueNotificationUseCase).sendDeadlineNotifications(any());
    }

    @Test
    void 이미_발송된_주차면_마감_알림을_발송하지_않는다() {
        alreadySent();

        scheduler.scheduleDeadline();

        verify(leagueNotificationUseCase, never()).sendDeadlineNotifications(any());
    }

    @Test
    void 이미_발송된_주차면_결과_알림을_발송하지_않는다() {
        alreadySent();

        scheduler.scheduleResult();

        verify(leagueNotificationUseCase, never()).sendResultNotifications(any());
    }

    private void alreadySent() {
        doThrow(new BaseException(CommonResponseCode.TOO_MANY_REQUESTS))
                .when(distributedLockFacade).checkAndSetCooldown(anyString(), anyLong());
    }
}
