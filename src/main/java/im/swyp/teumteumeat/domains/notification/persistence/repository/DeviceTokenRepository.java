package im.swyp.teumteumeat.domains.notification.persistence.repository;

import im.swyp.teumteumeat.domains.notification.persistence.entity.DeviceToken;
import im.swyp.teumteumeat.domains.notification.domain.constant.DeviceType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {
    boolean existsByTokenAndDeviceType(String token, DeviceType deviceType);

    void deleteByTokenAndDeviceType(String token, DeviceType deviceType);
}
