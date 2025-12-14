package im.swyp.teumteumeat.domains.user.application.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CommuteInfoRequest(
    @NotNull(message = "시간은 비어있을 수 없습니다.")
    LocalDateTime startTime,

    @NotNull(message = "시간은 비어있을 수 없습니다.")
    LocalDateTime endTime,

    @NotNull(message = "시간은 비어있을 수 없습니다.")
    int usageTime
) {
}
