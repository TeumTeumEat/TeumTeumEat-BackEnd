package im.swyp.teumteumeat.domains.user.application.usecase;

import im.swyp.teumteumeat.domains.goal.application.usecase.GoalUseCase;
import im.swyp.teumteumeat.domains.user.application.dto.request.CommuteInfoRequest;
import im.swyp.teumteumeat.domains.user.application.dto.request.NameRequest;
import im.swyp.teumteumeat.domains.user.application.dto.request.UserSettingsRequest;
import im.swyp.teumteumeat.domains.user.application.dto.response.CommuteInfoResponse;
import im.swyp.teumteumeat.domains.user.application.dto.response.CompletedResponse;
import im.swyp.teumteumeat.domains.user.application.dto.response.NameResponse;
import im.swyp.teumteumeat.domains.user.application.dto.response.UserSettingsResponse;
import im.swyp.teumteumeat.domains.user.application.mapper.CommuteInfoMapper;
import im.swyp.teumteumeat.domains.user.domain.constant.UserResponseCode;
import im.swyp.teumteumeat.domains.user.domain.service.UserService;
import im.swyp.teumteumeat.domains.user.persistence.entity.CommuteInfo;
import im.swyp.teumteumeat.domains.user.persistence.entity.UserEntity;
import im.swyp.teumteumeat.global.annotation.UseCase;
import im.swyp.teumteumeat.global.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserUseCase {

    private final UserService userService;
    private final GoalUseCase goalUseCase;

    public NameResponse getName(Long userId) {
        UserEntity user = userService.getUserById(userId);

        return NameResponse.builder()
                .name(user.getName())
                .build();
    }

    @Transactional
    public void updateName(Long userId, NameRequest request) {
        UserEntity user = userService.getUserById(userId);
        String name = request.name();
        userService.updateName(user, name);
    }

    public CommuteInfoResponse getCommuteInfo(Long userId) {
        UserEntity user = userService.getUserById(userId);
        CommuteInfo commuteInfo = userService.getCommuteInfo(user);
        CommuteInfoResponse response = CommuteInfoMapper.fromCommuteInfo(commuteInfo);

        if (response == null) {
            throw new BaseException(UserResponseCode.NOT_SET_COMMUTE_INFO);
        }
        return response;
    }

    @Transactional
    public void updateCommuteInfo(Long userId, CommuteInfoRequest request) {
        UserEntity user = userService.getUserById(userId);
        CommuteInfo commuteInfo = CommuteInfoMapper.toCommuteInfo(request);
        userService.updateCommuteInfo(user, commuteInfo);
    }

    @Transactional
    public CompletedResponse isOnboardingCompleted(Long userId) {
        UserEntity user = userService.getUserById(userId);
        if (
            user.isOnboardingCompleted() ||
            user.getName() != null &&
            userService.getCommuteInfo(user) != null &&
            !goalUseCase.getGoals(userId).goalResponses().isEmpty()
        ) {
            user.changeOnboardingCompleted(true);
            return CompletedResponse.builder().completed(true).build();
        } else {
            return CompletedResponse.builder().completed(false).build();
        }
    }

    public UserSettingsResponse getUserSettings(Long userId) {
        UserEntity user = userService.getUserById(userId);

        return UserSettingsResponse.builder()
                .pushEnabled(user.isPushEnabled())
                .build();
    }

    @Transactional
    public void updateUserSettings(Long userId, UserSettingsRequest request) {
        UserEntity user = userService.getUserById(userId);
        userService.updateSettings(user, request);
    }
}
