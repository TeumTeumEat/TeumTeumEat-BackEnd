package im.swyp.teumteumeat.domains.user.application.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record CommuteInfoRequest(
    @NotNull(message = "시간은 비어있을 수 없습니다.")
    LocalTime startTime,

    @NotNull(message = "시간은 비어있을 수 없습니다.")
    LocalTime endTime,

    @NotNull(message = "시간은 비어있을 수 없습니다.")
    int usageTime
) {
}
