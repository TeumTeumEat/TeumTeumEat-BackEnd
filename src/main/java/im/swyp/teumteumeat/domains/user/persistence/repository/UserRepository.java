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

    @Query("select distinct u from UserEntity u " +
            "join fetch u.deviceTokens " +
            "where (u.commuteInfo.startTime between :start and :end " +
            "   or u.commuteInfo.endTime between :start and :end) " +
            "and u.pushEnabled = true")
    List<UserEntity> findAllByCommuteTimeInRange(LocalTime start, LocalTime end);

    @Query("select u from UserEntity u left join fetch u.currentGoal g left join fetch g.category where u.id = :id")
    Optional<UserEntity> findWithCurrentGoalById(Long id);
}