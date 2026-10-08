package im.swyp.teumteumeat.domains.notification.application.usecase;

import im.swyp.teumteumeat.domains.notification.domain.constant.DeviceType;
import im.swyp.teumteumeat.domains.notification.domain.constant.NotificationProperties;
import im.swyp.teumteumeat.domains.notification.domain.constant.NotificationType;
import im.swyp.teumteumeat.domains.notification.persistence.entity.DeviceToken;
import im.swyp.teumteumeat.domains.user.domain.service.UserService;
import im.swyp.teumteumeat.domains.user.persistence.entity.UserEntity;
import im.swyp.teumteumeat.domains.userQuiz.domain.service.UserQuizService;
import im.swyp.teumteumeat.infra.fcm.domain.FcmService;
import im.swyp.teumteumeat.infra.fcm.dto.PushMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// 출퇴근 학습 알림의 대상 선정(오늘 퀴즈 풀이 여부)과 data.type 전달을 확인한다.
// FCM 발송은 목킹하고, 발송 요청된 푸시 메시지(토큰/data)를 검증한다.
class NotificationUseCaseTest {

    private static final LocalTime COMMUTE_TIME = LocalTime.of(8, 0);

    private UserService userService;
    private UserQuizService userQuizService;
    private FcmService fcmService;
    private NotificationUseCase notificationUseCase;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        userQuizService = mock(UserQuizService.class);
        fcmService = mock(FcmService.class);
        NotificationProperties properties = new NotificationProperties(
                "틈틈잇", 3, 0, List.of("{name}님, 퀴즈 풀 시간이에요"), List.of(), List.of());
        notificationUseCase = new NotificationUseCase(userQuizService, fcmService, userService, properties);

        when(userQuizService.calculateStreaksForUsers(any())).thenReturn(Map.of());
    }

    @Test
    void 출퇴근_알림은_오늘_퀴즈를_풀지_않은_유저에게_DAILY_QUIZ_타입으로_발송한다() {
        UserEntity unsolved = pushUser(1L, "token-1");
        UserEntity solved = pushUser(2L, "token-2");
        when(userService.getAllWithTokensByCommuteTime(any(), any())).thenReturn(List.of(unsolved, solved));
        when(userQuizService.getAllUsersByHasSolvedAnyQuizToday()).thenReturn(List.of(solved));

        notificationUseCase.sendNotifications(COMMUTE_TIME, COMMUTE_TIME.plusSeconds(59));

        List<PushMessage> sent = sentPushMessages();
        assertThat(sent).extracting(PushMessage::tokens).containsExactly(List.of("token-1"));
        assertThat(sent).extracting(PushMessage::data)
                .containsOnly(Map.of(NotificationType.DATA_KEY, NotificationType.DAILY_QUIZ.name()));
    }

    @SuppressWarnings("unchecked")
    private List<PushMessage> sentPushMessages() {
        ArgumentCaptor<List<PushMessage>> captor = ArgumentCaptor.forClass(List.class);
        verify(fcmService).send(captor.capture());
        return captor.getValue();
    }

    private UserEntity pushUser(Long userId, String token) {
        UserEntity user = mock(UserEntity.class);
        when(user.getId()).thenReturn(userId);
        when(user.getName()).thenReturn("유저" + userId);
        DeviceToken deviceToken = DeviceToken.builder().user(user).token(token).deviceType(DeviceType.ANDROID).build();
        when(user.getDeviceTokens()).thenReturn(List.of(deviceToken));
        return user;
    }
}
