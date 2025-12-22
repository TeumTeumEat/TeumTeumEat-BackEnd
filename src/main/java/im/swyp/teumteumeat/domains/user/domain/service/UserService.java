package im.swyp.teumteumeat.domains.user.domain.service;

import im.swyp.teumteumeat.domains.user.domain.constant.UserResponseCode;
import im.swyp.teumteumeat.domains.user.persistence.entity.CommuteInfo;
import im.swyp.teumteumeat.domains.user.persistence.entity.UserEntity;
import im.swyp.teumteumeat.domains.user.persistence.repository.UserRepository;
import im.swyp.teumteumeat.global.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    /* HELPER METHOD */
    private UserEntity getOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BaseException(UserResponseCode.NOT_FOUND_USER));
    }
}
