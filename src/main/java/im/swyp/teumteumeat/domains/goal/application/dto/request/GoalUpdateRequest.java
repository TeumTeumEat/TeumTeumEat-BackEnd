package im.swyp.teumteumeat.domains.goal.application.dto.request;

import im.swyp.teumteumeat.domains.goal.domain.constant.Difficulty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public record GoalUpdateRequest(

        @Schema(description = "목표 종료일")
        LocalDate endDate,

        @Schema(description = "난이도", example = "EASY/MEDIUM/HARD (Schema에서 ENUM 타입 확인)")
        Difficulty difficulty
) {
}
