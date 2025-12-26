package im.swyp.teumteumeat.domains.user.persistence.repository;

import im.swyp.teumteumeat.global.security.constant.SocialProvider;
import im.swyp.teumteumeat.domains.user.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findBySocialProviderAndSocialId(SocialProvider socialProvider, String socialId);

    @Query("SELECT u FROM UserEntity u WHERE u.commuteInfo.startTime BETWEEN :start AND :end")
    List<UserEntity> findAllByStartTimeBetween(LocalTime start, LocalTime end);

    @Query("SELECT u FROM UserEntity u WHERE u.commuteInfo.endTime BETWEEN :start AND :end")
    List<UserEntity> findAllByEndTimeBetween(LocalTime start, LocalTime end);
}