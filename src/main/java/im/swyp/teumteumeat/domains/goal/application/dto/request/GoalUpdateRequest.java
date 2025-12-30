package im.swyp.teumteumeat.domains.goal.application.dto.request;

import im.swyp.teumteumeat.domains.goal.domain.constant.Difficulty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record GoalUpdateRequest(

        @NotNull(message = "공부 기간은 비어있을 수 없습니다.")
        @Schema(description = "공부 기간", example = "1주")
        String studyPeriod,

        @Schema(description = "난이도", example = "EASY/MEDIUM/HARD (Schema에서 ENUM 타입 확인)")
        Difficulty difficulty,

        @Size(max = 30, message = "30자 이하이어야 합니다.")
        @Schema(description = "프롬프트", example = "~~식으로 문제를 내줘.")
        String prompt
) {
}
