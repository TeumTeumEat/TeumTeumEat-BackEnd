package im.swyp.teumteumeat.domains.user.application.dto.response;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalTime;

@Builder
public record CommuteInfoResponse(

        @Schema(description = "출근 시간", example = "08:00:00")
        LocalTime startTime,

        @Schema(description = "퇴근 시간", example = "18:00:00")
        LocalTime endTime,

        @Schema(description = "이용 시간", example = "10")
        int usageTime
) {
}
