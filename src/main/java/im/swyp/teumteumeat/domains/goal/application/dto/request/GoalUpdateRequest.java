package im.swyp.teumteumeat.domains.goal.application.dto.request;

import im.swyp.teumteumeat.domains.goal.domain.constant.Difficulty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record GoalUpdateRequest(

        @Schema(description = "목표 종료일")
        LocalDate endDate,

        @Schema(description = "난이도", example = "EASY/MEDIUM/HARD (Schema에서 ENUM 타입 확인)")
        Difficulty difficulty,

        @Size(max = 30, message = "30자 이하이어야 합니다.")
        @Schema(description = "프롬프트", example = "~~식으로 문제를 내줘.")
        String prompt
) {
}
