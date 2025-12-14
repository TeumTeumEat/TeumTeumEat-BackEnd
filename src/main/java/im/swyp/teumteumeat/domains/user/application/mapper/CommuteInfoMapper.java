package im.swyp.teumteumeat.domains.user.application.mapper;

import im.swyp.teumteumeat.domains.user.application.dto.request.CommuteInfoRequest;
import im.swyp.teumteumeat.domains.user.application.dto.response.CommuteInfoResponse;
import im.swyp.teumteumeat.domains.user.persistence.entity.CommuteInfo;

public class CommuteInfoMapper {
    public static CommuteInfo toCommuteInfo(CommuteInfoRequest request) {
        return CommuteInfo.builder()
                .startTime(request.startTime())
                .endTime(request.endTime())
                .usageTime(request.usageTime())
                .build();
    }

    public static CommuteInfoResponse fromCommuteInfo(CommuteInfo commuteInfo) {
        if (commuteInfo == null) {
            return null;
        }
        return new CommuteInfoResponse(
                commuteInfo.getStartTime(),
                commuteInfo.getEndTime(),
                commuteInfo.getUsageTime()
        );
    }
}
