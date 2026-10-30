package im.swyp.teumteumeat.domains.notification.application.mapper;

import im.swyp.teumteumeat.domains.notification.domain.constant.NotificationType;
import im.swyp.teumteumeat.domains.notification.persistence.entity.DeviceToken;
import im.swyp.teumteumeat.domains.user.persistence.entity.UserEntity;
import im.swyp.teumteumeat.infra.fcm.dto.PushMessage;

import java.util.Map;

public class PushMessageMapper {
    public static PushMessage toPushMessage(
            UserEntity user,
            String title,
            String body,
            Map<String, String> data
    ) {
        return new PushMessage(
                user.getDeviceTokens().stream().map(DeviceToken::getToken).toList(),
                title,
                body,
                data
        );
    }

    public static PushMessage toPushMessage(
            UserEntity user,
            String title,
            String body,
            NotificationType type
    ) {
        return toPushMessage(user, title, body, Map.of(NotificationType.DATA_KEY, type.name()));
    }
}
