package im.swyp.teumteumeat.domains.notification.application.usecase;

import im.swyp.teumteumeat.domains.notification.application.dto.request.NotificationRequest;
import im.swyp.teumteumeat.domains.notification.domain.service.DeviceTokenService;
import im.swyp.teumteumeat.domains.notification.persistence.entity.DeviceToken;
import im.swyp.teumteumeat.domains.user.domain.service.UserService;
import im.swyp.teumteumeat.domains.user.persistence.entity.UserEntity;
import im.swyp.teumteumeat.domains.userQuiz.domain.service.UserQuizService;
import im.swyp.teumteumeat.global.annotation.UseCase;
import im.swyp.teumteumeat.domains.notification.domain.constant.NotificationProperties;
import im.swyp.teumteumeat.infra.fcm.domain.FcmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class NotificationUseCase {

    private final DeviceTokenService deviceTokenService;
    private final UserQuizService userQuizService;
    private final FcmService fcmService;
    private final UserService userService;
    private final NotificationProperties notificationProperties;
    private static final Random random = new Random();

    public void sendNotifications(LocalTime now, LocalTime minuteEnd) {
        // 시간 범위에 속하는 출퇴근 시간인 유저를 모두 불러옴
        List<UserEntity> users = userService.getAllWithTokensByCommuteTime(now, minuteEnd);

        // 이미 문제를 푼 유저는 리스트에서 제외
        Set<Long> solvedUserIds = userQuizService.getAllUsersByHasSolvedAnyQuizToday().stream()
                .map(UserEntity::getId)
                .collect(Collectors.toSet());
        users = users.stream()
                .filter(u -> !solvedUserIds.contains(u.getId()))
                .toList();

        // 대상 유저의 스트릭 수 한번에 조회
        List<Long> userIds = users.stream().map(UserEntity::getId).toList();
        Map<Long, Integer> streakMap = userQuizService.calculateStreaksForUsers(userIds);

        String title = "틈틈잇 준비 완료!";

        users.forEach(user -> {
            String name = user.getName();
            int streak = streakMap.getOrDefault(user.getId(), 0);

            List<String> messagePool = selectMessagePool(streak);
            String template = getRandomMessageFromPool(messagePool);
            String body = template
                    .replace("{name}", name)
                    .replace("{streak}", String.valueOf(streak));

            // 유저의 등록된 토큰을 모두 불러옴
            List<DeviceToken> tokens = user.getDeviceTokens();
            tokens.forEach(token -> {
                // 알림 전송
                fcmService.sendNotification(token.getToken(), title, body, null);
            });
        });
    }

    public void sendNotificationTest(NotificationRequest request, Long userId) {
        List<DeviceToken> tokens = deviceTokenService.getAllTokenByUserId(userId);
        tokens.forEach(token -> fcmService.sendNotification(
                token.getToken(),
                request.title(),
                request.body(),
                request.data()
        ));
    }

    private List<String> selectMessagePool(int currentStreak) {
        // 스트릭이 임계값을 넘었고, 설정된 확률에 따라 스트릭 메시지 전송 여부 결정
        if (currentStreak >= notificationProperties.getStreakThreshold() &&
                random.nextInt(100) < notificationProperties.getStreakMessageRatio()) {
            return notificationProperties.getStreakMessages();
        }
        return notificationProperties.getDefaultMessages();
    }

    private String getRandomMessageFromPool(List<String> messages) {
        if (messages == null || messages.isEmpty()) {
            return "오늘의 냠냠 지식이 도착했습니다! 지금 바로 확인해보세요. 🏆";
        }
        return messages.get(random.nextInt(messages.size()));
    }
}
