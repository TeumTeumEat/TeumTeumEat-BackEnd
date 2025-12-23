package im.swyp.teumteumeat.domains.user.application.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalTime;

public record CommuteInfoRequest(

    @NotNull(message = "시간은 비어있을 수 없습니다.")
    @Schema(description = "출근 시간", example = "08:00:00")
    LocalTime startTime,

    @NotNull(message = "시간은 비어있을 수 없습니다.")
    @Schema(description = "퇴근 시간", example = "18:00:00")
    LocalTime endTime,

    @NotNull(message = "이용 시간은 비어있을 수 없습니다.")
    @Schema(description = "이용 시간", example = "10")
    @Min(value = 5, message = "이용 시간은 5분 이상으로만 설정 가능합니다.")
    @Max(value = 1440, message = "이용 시간은 1440분 이하로만 설정 가능합니다.")
    int usageTime
) {
}
