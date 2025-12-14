package im.swyp.teumteumeat.domains.user.domain.service;

import im.swyp.teumteumeat.domains.user.application.dto.request.CommuteInfoRequest;
import im.swyp.teumteumeat.domains.user.application.dto.response.CommuteInfoResponse;
import im.swyp.teumteumeat.domains.user.application.mapper.CommuteInfoMapper;
import im.swyp.teumteumeat.domains.user.persistence.entity.CommuteInfo;
import im.swyp.teumteumeat.domains.user.persistence.entity.UserEntity;
import im.swyp.teumteumeat.domains.user.persistence.repository.UserRepository;
import im.swyp.teumteumeat.global.common.CommonResponseCode;
import im.swyp.teumteumeat.global.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public CommuteInfoResponse getCommuteInfo(UserEntity user) {
        return CommuteInfoMapper.fromCommuteInfo(user.getCommuteInfo());
    }

    public void updateCommuteInfo(UserEntity user, CommuteInfoRequest request) {
        CommuteInfo commuteInfo = CommuteInfoMapper.toCommuteInfo(request);
        user.updateCommuteInfo(commuteInfo);
    }

    public UserEntity getUserById(Long id) {
        return getOrThrow(id);
    }

    /* HELPER METHOD */
    private UserEntity getOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BaseException(CommonResponseCode.NOT_FOUND));
    }
}
