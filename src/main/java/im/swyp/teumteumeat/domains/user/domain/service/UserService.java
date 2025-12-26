package im.swyp.teumteumeat.domains.user.domain.service;

import im.swyp.teumteumeat.domains.user.application.dto.request.UserSettingsRequest;
import im.swyp.teumteumeat.domains.user.domain.constant.UserResponseCode;
import im.swyp.teumteumeat.domains.user.persistence.entity.CommuteInfo;
import im.swyp.teumteumeat.domains.user.persistence.entity.UserEntity;
import im.swyp.teumteumeat.domains.user.persistence.repository.UserRepository;
import im.swyp.teumteumeat.global.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserEntity getUserById(Long userId) {
        return getOrThrow(userId);
    }

    public List<UserEntity> getAllByCommuteInfo(LocalTime now, LocalTime minuteEnd) {
        List<UserEntity> targets = new ArrayList<>();
        targets.addAll(userRepository.findAllByStartTimeBetween(now, minuteEnd));
        targets.addAll(userRepository.findAllByEndTimeBetween(now, minuteEnd));
        return targets;
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

    /* HELPER METHOD */
    private UserEntity getOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BaseException(UserResponseCode.NOT_FOUND_USER));
    }
}
