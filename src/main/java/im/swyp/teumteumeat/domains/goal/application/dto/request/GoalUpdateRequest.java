package im.swyp.teumteumeat.domains.goal.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public record GoalUpdateRequest(

        @Schema(description = "목표 종료일")
        LocalDate endDate
) {
}
