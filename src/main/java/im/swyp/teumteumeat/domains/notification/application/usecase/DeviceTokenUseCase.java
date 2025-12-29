package im.swyp.teumteumeat.domains.notification.application.usecase;

import im.swyp.teumteumeat.domains.notification.application.mapper.DeviceTokenMapper;
import im.swyp.teumteumeat.domains.notification.domain.service.DeviceTokenService;
import im.swyp.teumteumeat.domains.notification.persistence.entity.DeviceToken;
import im.swyp.teumteumeat.domains.user.domain.service.UserService;
import im.swyp.teumteumeat.domains.user.persistence.entity.UserEntity;
import im.swyp.teumteumeat.global.annotation.UseCase;
import im.swyp.teumteumeat.domains.notification.application.dto.request.DeviceTokenRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeviceTokenUseCase {

    private final DeviceTokenService deviceTokenService;
    private final UserService userService;

    @Transactional
    public void registerDeviceToken(Long userId, DeviceTokenRequest request) {
        UserEntity user = userService.getUserById(userId);

        DeviceToken deviceToken = DeviceTokenMapper.toDeviceToken(user, request);
        deviceTokenService.registerDeviceToken(deviceToken);
    }

    @Transactional
    public void unregisterDeviceToken(Long userId, @Valid DeviceTokenRequest request) {
        UserEntity user = userService.getUserById(userId);
        String token = request.token();

        deviceTokenService.unregisterDeviceToken(user, token);
    }
}
