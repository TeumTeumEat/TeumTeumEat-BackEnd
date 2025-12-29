package im.swyp.teumteumeat.domains.notification.domain.service;

import im.swyp.teumteumeat.domains.notification.persistence.entity.DeviceToken;
import im.swyp.teumteumeat.domains.notification.domain.constant.DeviceType;
import im.swyp.teumteumeat.domains.notification.persistence.repository.DeviceTokenRepository;
import im.swyp.teumteumeat.domains.user.persistence.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceTokenService {

    private final DeviceTokenRepository deviceTokenRepository;

    public void registerDeviceToken(DeviceToken deviceToken) {
        String token = deviceToken.getToken();
        DeviceType deviceType = deviceToken.getDeviceType();

        if (!deviceTokenRepository.existsByTokenAndDeviceType(token, deviceType)) {
            deviceTokenRepository.save(deviceToken);
        }
    }

    public void unregisterDeviceToken(UserEntity user, String token) {
        deviceTokenRepository.deleteByUserAndToken(user, token);
    }

    // Test Method
    public List<DeviceToken> getAllTokenByUserId(Long userId) {
        return deviceTokenRepository.findAllByUserId(userId);
    }
}
