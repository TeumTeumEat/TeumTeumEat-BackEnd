package im.swyp.teumteumeat.domains.user.application.usecase;

import im.swyp.teumteumeat.domains.user.application.dto.request.CommuteInfoRequest;
import im.swyp.teumteumeat.domains.user.application.dto.response.CommuteInfoResponse;
import im.swyp.teumteumeat.domains.user.domain.constant.UserResponseCode;
import im.swyp.teumteumeat.domains.user.domain.service.UserService;
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

    public CommuteInfoResponse getCommuteInfo(Long userId) {
        UserEntity user = userService.getUserById(userId);
        CommuteInfoResponse response = userService.getCommuteInfo(user);

        if (response == null) {
            throw new BaseException(UserResponseCode.NOT_SET_COMMUTE_INFO);
        }
        return response;
    }

    @Transactional
    public void updateCommuteInfo(Long userId, CommuteInfoRequest request) {
        UserEntity user = userService.getUserById(userId);
        userService.updateCommuteInfo(user, request);
    }
}
