package im.swyp.teumteumeat.domains.notification.application.usecase;

import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import im.swyp.teumteumeat.domains.notification.application.dto.request.NotificationRequest;
import im.swyp.teumteumeat.domains.notification.domain.constant.NotificationProperties.FixedStreakMessage;
import im.swyp.teumteumeat.domains.notification.persistence.entity.DeviceToken;
import im.swyp.teumteumeat.domains.user.domain.service.UserService;
import im.swyp.teumteumeat.domains.user.persistence.entity.UserEntity;
import im.swyp.teumteumeat.domains.userQuiz.domain.service.UserQuizService;
import im.swyp.teumteumeat.global.annotation.UseCase;
import im.swyp.teumteumeat.domains.notification.domain.constant.NotificationProperties;
import im.swyp.teumteumeat.infra.fcm.domain.service.FcmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class NotificationUseCase {

    private final UserQuizService userQuizService;
    private final FcmService fcmService;
    private final UserService userService;
    private final NotificationProperties notificationProperties;
    private static final Random random = new Random();
    private static final String PUSH_TITLE = "틈틈잇 준비 완료!";

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

        List<Message> messagesToBatch = new ArrayList<>();
        List<String> tokensToBatch = new ArrayList<>();

        for (UserEntity user : users) {
            int streak = streakMap.getOrDefault(user.getId(), 0);
            String body = getMessageByUserStreak(streak)
                    .replace("{name}", user.getName())
                    .replace("{streak}", String.valueOf(streak));

            for (DeviceToken deviceToken : user.getDeviceTokens()) {
                Message message = Message.builder()
                        .setToken(deviceToken.getToken())
                        .setNotification(Notification.builder()
                                .setTitle(PUSH_TITLE)
                                .setBody(body)
                                .build())
                        .build();
                messagesToBatch.add(message);
                tokensToBatch.add(deviceToken.getToken());
            }
        }

        fcmService.sendBatchMessages(messagesToBatch, tokensToBatch, false);
    }

    @Transactional
    public void sendNotificationTest(NotificationRequest request, Long userId) {
        UserEntity user = userService.getUserById(userId);

        List<Message> messagesToBatch = new ArrayList<>();
        List<String> tokensToBatch = new ArrayList<>();
        for (DeviceToken deviceToken : user.getDeviceTokens()) {
            Message message = Message.builder()
                    .setToken(deviceToken.getToken())
                    .setNotification(Notification.builder()
                            .setTitle(request.title())
                            .setBody(request.body())
                            .build())
                    .putAllData((request.data() != null) ? request.data() : Collections.emptyMap())
                    .build();
            messagesToBatch.add(message);
            tokensToBatch.add(deviceToken.getToken());
        }

        fcmService.sendBatchMessages(messagesToBatch, tokensToBatch, false);
    }

    private String getMessageByUserStreak(int userStreak) {
        // 스트릭 메시지 발송 기준을 넘었고, 스트릭 메시지와 일반 메시지를 비율에 따라 적절히 섞어 보냄
        if (userStreak >= notificationProperties.getStreakThreshold() &&
                random.nextInt(100) < notificationProperties.getStreakMessageRatio()) {
            // 특정 스트릭 일 수의 메시지가 있다면 찾아서 반환
            List<FixedStreakMessage> fixedStreakMessages = notificationProperties.getFixedStreakMessages();
            for (FixedStreakMessage m : fixedStreakMessages) {
                if (m.getDate() == userStreak) {
                    return m.getMessage();
                }
            }

            // 아니라면 스트릭 메시지 목록 중 랜덤 반환
            List<String> randomStreakMessages = notificationProperties.getRandomStreakMessages();
            int randomIndex = random.nextInt(randomStreakMessages.size());
            return randomStreakMessages.get(randomIndex);
        }

        // 위의 경우가 아닐 경우 기본 메시지 중 랜덤 반환
        List<String> defaultMessages = notificationProperties.getDefaultMessages();
        return defaultMessages.get(random.nextInt(defaultMessages.size()));
    }
}
