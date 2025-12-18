package im.swyp.teumteumeat.domains.goal.application.dto.request;

import im.swyp.teumteumeat.domains.goal.domain.constant.GoalType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record GoalCreateRequest(

        @NotNull(message = "종류는 비어있을 수 없습니다.")
        @Schema(description = "목표 타입", example = "CATEGORY (Schema에서 ENUM 타입 확인)")
        GoalType type,

        @NotNull(message = "날짜는 비어있을 수 없습니다.")
        @Schema(description = "목표 종료일")
        LocalDate endDate,

        @Schema(description = "카테고리 ID", example = "1")
        Long categoryId
) {
}
