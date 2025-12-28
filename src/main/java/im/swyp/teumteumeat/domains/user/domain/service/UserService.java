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

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserEntity getUserById(Long userId) {
        return getOrThrow(userId);
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
