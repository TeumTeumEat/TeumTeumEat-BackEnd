package im.swyp.teumteumeat.domains.notification.application.mapper;

import im.swyp.teumteumeat.domains.notification.application.dto.request.DeviceTokenRequest;
import im.swyp.teumteumeat.domains.notification.persistence.entity.DeviceToken;
import im.swyp.teumteumeat.domains.user.persistence.entity.UserEntity;

public class DeviceTokenMapper {
    public static DeviceToken toDeviceToken(
            UserEntity user,
            DeviceTokenRequest request
    ) {
        return DeviceToken.builder()
                .user(user)
                .token(request.token())
                .deviceType(request.deviceType())
                .build();
    }
}
