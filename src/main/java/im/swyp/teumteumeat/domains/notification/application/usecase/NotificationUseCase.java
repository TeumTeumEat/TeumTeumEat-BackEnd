package im.swyp.teumteumeat.domains.notification.application.usecase;

import im.swyp.teumteumeat.domains.notification.application.dto.request.NotificationRequest;
import im.swyp.teumteumeat.domains.notification.domain.service.DeviceTokenService;
import im.swyp.teumteumeat.domains.notification.persistence.entity.DeviceToken;
import im.swyp.teumteumeat.domains.user.domain.service.UserService;
import im.swyp.teumteumeat.domains.user.persistence.entity.UserEntity;
import im.swyp.teumteumeat.global.annotation.UseCase;
import im.swyp.teumteumeat.infra.fcm.domain.FcmService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

@UseCase
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationUseCase {

    private final DeviceTokenService deviceTokenService;
    private final FcmService fcmService;
    private final UserService userService;

    public void sendNotifications(LocalTime now, LocalTime minuteEnd) {
        // 시간 범위에 속하는 출퇴근 시간인 유저를 모두 불러옴
        List<UserEntity> users = userService.getAllByCommuteInfo(now, minuteEnd);

        String title = "틈틈잇 준비 완료!";
        String body = "지금 바로 학습해보세요!";

        users.forEach(user -> {
            if (user.isPushEnabled()) {
                // 유저의 등록된 토큰을 모두 불러옴
                List<DeviceToken> tokens = deviceTokenService.getAllTokenByUserId(user.getId());
                tokens.forEach(token -> {
                    String tokenValue = token.getToken();
                    // 알림 전송
                    fcmService.sendNotification(tokenValue, title, body, null);
                });
            }
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
}
