package im.swyp.teumteumeat.domains.notification.persistence.repository;

import im.swyp.teumteumeat.domains.notification.persistence.entity.DeviceToken;
import im.swyp.teumteumeat.domains.notification.domain.constant.DeviceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {

    Optional<DeviceToken> findByTokenAndDeviceType(String token, DeviceType deviceType);

    void deleteByTokenAndDeviceType(String token, DeviceType deviceType);
}
