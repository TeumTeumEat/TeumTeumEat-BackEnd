package im.swyp.teumteumeat.domains.notification.domain.service;

import im.swyp.teumteumeat.domains.notification.persistence.entity.DeviceToken;
import im.swyp.teumteumeat.domains.notification.domain.constant.DeviceType;
import im.swyp.teumteumeat.domains.notification.persistence.repository.DeviceTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    public void unregisterDeviceToken(DeviceToken deviceToken) {
        String token = deviceToken.getToken();
        DeviceType deviceType = deviceToken.getDeviceType();

        deviceTokenRepository.deleteByTokenAndDeviceType(token, deviceType);
    }
}
