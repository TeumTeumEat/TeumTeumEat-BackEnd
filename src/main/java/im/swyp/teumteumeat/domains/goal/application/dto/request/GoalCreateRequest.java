package im.swyp.teumteumeat.domains.goal.application.dto.request;

import im.swyp.teumteumeat.domains.goal.domain.constant.GoalType;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record GoalCreateRequest(

        @NotNull(message = "종류는 비어있을 수 없습니다.")
        GoalType type,

        @NotNull(message = "날짜는 비어있을 수 없습니다.")
        LocalDate endDate,

        Long categoryId
) {
}
