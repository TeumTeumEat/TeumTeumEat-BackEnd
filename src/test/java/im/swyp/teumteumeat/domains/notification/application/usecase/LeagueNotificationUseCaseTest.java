package im.swyp.teumteumeat.domains.notification.application.usecase;

import im.swyp.teumteumeat.domains.league.application.component.LeagueRankingProvider;
import im.swyp.teumteumeat.domains.league.domain.vo.LeagueParticipant;
import im.swyp.teumteumeat.domains.league.domain.vo.LeagueRanking;
import im.swyp.teumteumeat.domains.league.domain.vo.LeagueWeek;
import im.swyp.teumteumeat.domains.notification.domain.constant.DeviceType;
import im.swyp.teumteumeat.domains.notification.domain.constant.LeagueNotificationProperties;
import im.swyp.teumteumeat.domains.notification.domain.constant.NotificationType;
import im.swyp.teumteumeat.domains.notification.persistence.entity.DeviceToken;
import im.swyp.teumteumeat.domains.user.domain.service.UserService;
import im.swyp.teumteumeat.domains.user.persistence.entity.UserEntity;
import im.swyp.teumteumeat.infra.fcm.domain.FcmService;
import im.swyp.teumteumeat.infra.fcm.dto.PushMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// 주간 리그 푸시 알림의 대상 선정(순위 유무)과 문구 치환을 확인한다.
// FCM 발송은 목킹하고, 발송 요청된 푸시 메시지(토큰/본문/data)와 조회한 리그 주차를 검증한다.
class LeagueNotificationUseCaseTest {

    // 2026-10-04(일) 20:00 → 이번 주 시작 2026-09-28(월)
    private static final LocalDateTime SUNDAY_EVENING = LocalDateTime.of(2026, 10, 4, 20, 0);
    // 2026-10-05(월) 08:00 → 지난주 시작 2026-09-28(월)
    private static final LocalDateTime MONDAY_MORNING = LocalDateTime.of(2026, 10, 5, 8, 0);

    private LeagueRankingProvider leagueRankingProvider;
    private UserService userService;
    private FcmService fcmService;
    private LeagueNotificationUseCase leagueNotificationUseCase;

    private final List<LeagueParticipant> participants = new ArrayList<>();
    private final List<UserEntity> pushUsers = new ArrayList<>();

    @BeforeEach
    void setUp() {
        leagueRankingProvider = mock(LeagueRankingProvider.class);
        userService = mock(UserService.class);
        fcmService = mock(FcmService.class);
        LeagueNotificationProperties properties = new LeagueNotificationProperties(
                "틈틈잇", "마감 4시간 전! 현재 {rank}위", "마감 4시간 전!", "결과를 확인해 보세요");
        leagueNotificationUseCase = new LeagueNotificationUseCase(
                leagueRankingProvider, userService, fcmService, properties);

        when(leagueRankingProvider.getRanking(any())).thenAnswer(inv -> new LeagueRanking(participants));
        when(userService.getAllWithTokensByPushEnabled()).thenReturn(pushUsers);
    }

    @Test
    void 순위가_있으면_마감_문구에_순위를_치환한다() {
        assertThat(leagueNotificationUseCase.resolveDeadlineBody(3)).isEqualTo("마감 4시간 전! 현재 3위");
    }

    @Test
    void 순위가_없으면_마감_기본_문구를_사용한다() {
        assertThat(leagueNotificationUseCase.resolveDeadlineBody(null)).isEqualTo("마감 4시간 전!");
    }

    @Test
    void 마감_알림은_순위와_관계없이_푸시_허용_유저_전원에게_발송한다() {
        participant(1L);
        pushUser(1L, "token-1a", "token-1b");
        pushUser(2L, "token-2");

        leagueNotificationUseCase.sendDeadlineNotifications(SUNDAY_EVENING);

        List<PushMessage> sent = sentPushMessages();
        assertThat(sent).extracting(PushMessage::tokens)
                .containsExactly(List.of("token-1a", "token-1b"), List.of("token-2"));
        assertThat(sent).extracting(PushMessage::body)
                .containsExactly("마감 4시간 전! 현재 1위", "마감 4시간 전!");
        assertThat(sent).extracting(PushMessage::data)
                .containsOnly(Map.of(NotificationType.DATA_KEY, NotificationType.LEAGUE_DEADLINE.name()));
    }

    @Test
    void 마감_알림은_이번_주_랭킹을_조회한다() {
        leagueNotificationUseCase.sendDeadlineNotifications(SUNDAY_EVENING);

        assertThat(requestedWeek().weekStartDate()).isEqualTo(SUNDAY_EVENING.toLocalDate().minusDays(6));
    }

    @Test
    void 결과_알림은_지난주_순위에_든_유저에게만_발송한다() {
        participant(1L);
        pushUser(1L, "token-1");
        pushUser(2L, "token-2");

        leagueNotificationUseCase.sendResultNotifications(MONDAY_MORNING);

        List<PushMessage> sent = sentPushMessages();
        assertThat(sent).extracting(PushMessage::tokens).containsExactly(List.of("token-1"));
        assertThat(sent).extracting(PushMessage::body).containsExactly("결과를 확인해 보세요");
        assertThat(sent).extracting(PushMessage::data)
                .containsOnly(Map.of(NotificationType.DATA_KEY, NotificationType.LEAGUE_RESULT.name()));
    }

    @Test
    void 결과_알림은_지난주_랭킹을_조회한다() {
        leagueNotificationUseCase.sendResultNotifications(MONDAY_MORNING);

        assertThat(requestedWeek().weekStartDate()).isEqualTo(MONDAY_MORNING.toLocalDate().minusWeeks(1));
    }

    @SuppressWarnings("unchecked")
    private List<PushMessage> sentPushMessages() {
        ArgumentCaptor<List<PushMessage>> captor = ArgumentCaptor.forClass(List.class);
        verify(fcmService).send(captor.capture());
        return captor.getValue();
    }

    private LeagueWeek requestedWeek() {
        ArgumentCaptor<LeagueWeek> captor = ArgumentCaptor.forClass(LeagueWeek.class);
        verify(leagueRankingProvider).getRanking(captor.capture());
        return captor.getValue();
    }

    private void participant(Long userId) {
        participants.add(new LeagueParticipant(userId, "유저" + userId, SUNDAY_EVENING, 1, 0, 0));
    }

    private void pushUser(Long userId, String... tokens) {
        UserEntity user = mock(UserEntity.class);
        when(user.getId()).thenReturn(userId);
        List<DeviceToken> deviceTokens = Arrays.stream(tokens)
                .map(token -> DeviceToken.builder().user(user).token(token).deviceType(DeviceType.ANDROID).build())
                .toList();
        when(user.getDeviceTokens()).thenReturn(deviceTokens);
        pushUsers.add(user);
    }
}
