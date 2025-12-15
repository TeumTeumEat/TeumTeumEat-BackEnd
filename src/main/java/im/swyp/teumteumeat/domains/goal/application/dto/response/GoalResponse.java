package im.swyp.teumteumeat.domains.goal.application.dto.response;

import im.swyp.teumteumeat.domains.category.application.dto.response.CategoryResponse;
import im.swyp.teumteumeat.domains.goal.domain.constant.GoalType;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record GoalResponse(

        Long goalId,

        GoalType type,

        LocalDate startDate,

        LocalDate endDate,

        CategoryResponse category
) {
}
