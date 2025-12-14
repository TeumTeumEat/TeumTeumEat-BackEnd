package im.swyp.teumteumeat.domains.user.application.dto.response;


import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record CommuteInfoResponse(

        LocalDateTime startTime,

        LocalDateTime endTime,

        int usageTime
) {
}
