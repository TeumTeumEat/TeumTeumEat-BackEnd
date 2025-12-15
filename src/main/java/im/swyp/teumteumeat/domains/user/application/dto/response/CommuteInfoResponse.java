package im.swyp.teumteumeat.domains.user.application.dto.response;


import lombok.Builder;

import java.time.LocalTime;

@Builder
public record CommuteInfoResponse(

        LocalTime startTime,

        LocalTime endTime,

        int usageTime
) {
}
