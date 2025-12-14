package im.swyp.teumteumeat.domains.user.persistence.repository;

import im.swyp.teumteumeat.global.security.constant.SocialProvider;
import im.swyp.teumteumeat.domains.user.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findBySocialProviderAndSocialId(SocialProvider socialProvider, String socialId);
}