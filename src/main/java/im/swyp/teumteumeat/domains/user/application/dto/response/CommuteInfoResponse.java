package im.swyp.teumteumeat.domains.user.application.dto.response;


import java.time.LocalDateTime;

public record CommuteInfoResponse(

        LocalDateTime startTime,

        LocalDateTime endTime,

        int usageTime
) {
}
