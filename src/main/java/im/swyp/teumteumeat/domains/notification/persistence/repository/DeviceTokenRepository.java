package im.swyp.teumteumeat.domains.notification.persistence.repository;

import im.swyp.teumteumeat.domains.notification.persistence.entity.DeviceToken;
import im.swyp.teumteumeat.domains.notification.domain.constant.DeviceType;
import im.swyp.teumteumeat.domains.user.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {

    List<DeviceToken> findAllByUserId(Long userId);

    boolean existsByTokenAndDeviceType(String token, DeviceType deviceType);

    void deleteByUserAndToken(UserEntity user, String token);
}
