package im.swyp.teumteumeat.domains.notification.domain.service;

import im.swyp.teumteumeat.domains.notification.persistence.entity.DeviceToken;
import im.swyp.teumteumeat.domains.notification.domain.constant.DeviceType;
import im.swyp.teumteumeat.domains.notification.persistence.repository.DeviceTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DeviceTokenService {

    private final DeviceTokenRepository deviceTokenRepository;

    public void registerDeviceToken(DeviceToken deviceToken) {
        String token = deviceToken.getToken();
        DeviceType deviceType = deviceToken.getDeviceType();

        Optional<DeviceToken> existingTokenOpt = deviceTokenRepository.findByTokenAndDeviceType(token, deviceType);

        // 해당 기기 토큰이 이미 DB에 있다면
        if (existingTokenOpt.isPresent()) {
            // 등록된 기기 토큰의 소유자가 일치하지 않는다면, 기존 토큰 삭제 후 현재 유저로 등록
            DeviceToken existingToken = existingTokenOpt.get();
            if (!existingToken.getUser().getId().equals(deviceToken.getUser().getId())) {
                deviceTokenRepository.deleteByTokenAndDeviceType(token, deviceType);
                deviceTokenRepository.save(deviceToken);
            }
            // 일치하다면 아무것도 하지 않음
        } else {
            // 신규 등록
            deviceTokenRepository.save(deviceToken);
        }
    }

    public void unregisterDeviceToken(String token, DeviceType deviceType) {
        deviceTokenRepository.deleteByTokenAndDeviceType(token, deviceType);
    }

    @Transactional
    public void deleteInvalidTokens(List<String> tokens) {
        deviceTokenRepository.deleteAllByTokenIn(tokens);
    }
}
