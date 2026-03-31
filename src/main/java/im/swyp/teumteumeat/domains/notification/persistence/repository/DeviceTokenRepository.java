package im.swyp.teumteumeat.domains.notification.persistence.repository;

import im.swyp.teumteumeat.domains.notification.persistence.entity.DeviceToken;
import im.swyp.teumteumeat.domains.notification.domain.constant.DeviceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {

    Optional<DeviceToken> findByTokenAndDeviceType(String token, DeviceType deviceType);

    void deleteByTokenAndDeviceType(String token, DeviceType deviceType);

    @Modifying(clearAutomatically = true)
    @Query("delete from DeviceToken d where d.token in :tokens")
    void deleteAllByTokenIn(List<String> tokens);
}
