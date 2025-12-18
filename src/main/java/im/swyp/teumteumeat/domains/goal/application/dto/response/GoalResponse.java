package im.swyp.teumteumeat.domains.goal.application.dto.response;

import im.swyp.teumteumeat.domains.category.application.dto.response.CategoryResponse;
import im.swyp.teumteumeat.domains.goal.domain.constant.GoalType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record GoalResponse(

        @Schema(description = "목표 ID", example = "1")
        Long goalId,

        @Schema(description = "목표 타입", example = "CATEGORY (Schema에서 ENUM 타입 확인)")
        GoalType type,

        @Schema(description = "목표 시작일(생성일)")
        LocalDate startDate,

        @Schema(description = "목표 종료일")
        LocalDate endDate,

        CategoryResponse category
) {
}
