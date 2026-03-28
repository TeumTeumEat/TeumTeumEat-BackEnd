package im.swyp.teumteumeat.domains.user.domain.service;

import im.swyp.teumteumeat.domains.user.application.dto.request.UserSettingsRequest;
import im.swyp.teumteumeat.domains.user.domain.constant.UserResponseCode;
import im.swyp.teumteumeat.domains.user.persistence.entity.CommuteInfo;
import im.swyp.teumteumeat.domains.user.persistence.entity.UserEntity;
import im.swyp.teumteumeat.domains.user.persistence.repository.UserRepository;
import im.swyp.teumteumeat.global.exception.BaseException;
import im.swyp.teumteumeat.global.security.constant.SocialProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserEntity getUserById(Long userId) {
        return getOrThrow(userId);
    }

    public UserEntity getUserWithCurrentGoal(Long userId) {
        return userRepository.findWithCurrentGoalById(userId)
                .orElseThrow(() -> new BaseException(UserResponseCode.NOT_FOUND_USER));
    }

    @Transactional(readOnly = true)
    public List<UserEntity> getAllWithTokensByCommuteTime(LocalTime now, LocalTime minuteEnd) {
        return userRepository.findAllByCommuteTimeInRange(now, minuteEnd);
    }

    public void updateName(UserEntity user, String name) {
        user.updateName(name);
    }

    public CommuteInfo getCommuteInfo(UserEntity user) {
        return user.getCommuteInfo();
    }

    public void updateCommuteInfo(UserEntity user, CommuteInfo commuteInfo) {
        user.updateCommuteInfo(commuteInfo);
    }

    public void updateSettings(UserEntity user, UserSettingsRequest request) {
        user.updateSettings(request);
    }

    public void deleteUser(UserEntity user) {
        userRepository.delete(user);
    }

    @Transactional
    public void updateSocialRefreshToken(Long userId, String token) {
        UserEntity user = getOrThrow(userId);
        user.updateSocialRefreshToken(token);
        userRepository.save(user); // Force update to ensure persistence
    }

    @Transactional
    public boolean updateAndGetOnboardingCompleted(Long userId) {
        UserEntity user = getOrThrow(userId);
        return user.updateAndGetOnboardingCompleted();
    }

    @Transactional
    public void resetQuizGuide(Long userId) {
        UserEntity user = getOrThrow(userId);
        user.resetQuizGuide();
    }

    /* HELPER METHOD */
    private UserEntity getOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BaseException(UserResponseCode.NOT_FOUND_USER));
    }

    public Optional<UserEntity> findBySocialProviderAndSocialId(SocialProvider provider, String socialId) {
        return userRepository.findBySocialProviderAndSocialId(provider, socialId);
    }

    public UserEntity getOrSaveUser(String name, SocialProvider provider, String socialId, String email) {
        return userRepository
                .findBySocialProviderAndSocialId(provider, socialId)
                .orElseGet(() -> userRepository.save(
                        UserEntity.socialSignup(
                                name,
                                email,
                                provider,
                                socialId)));
    }
}
