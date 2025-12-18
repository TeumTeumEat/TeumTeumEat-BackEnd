package im.swyp.teumteumeat.domains.user.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record CommuteInfoRequest(

    @NotNull(message = "시간은 비어있을 수 없습니다.")
    @Schema(description = "출근 시간", example = "08:00:00")
    LocalTime startTime,

        @Schema(description = "퇴근 시간", example = "18:00:00")
    @NotNull(message = "시간은 비어있을 수 없습니다.")
    LocalTime endTime,

    @Schema(description = "이용 시간", example = "10")
    @NotNull(message = "시간은 비어있을 수 없습니다.")
    int usageTime
) {
}
