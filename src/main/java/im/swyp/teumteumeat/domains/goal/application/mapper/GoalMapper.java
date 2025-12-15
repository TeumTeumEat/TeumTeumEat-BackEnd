package im.swyp.teumteumeat.domains.goal.application.mapper;

import im.swyp.teumteumeat.domains.goal.application.dto.request.GoalRequest;
import im.swyp.teumteumeat.domains.goal.application.dto.response.GoalListResponse;
import im.swyp.teumteumeat.domains.goal.application.dto.response.GoalResponse;
import im.swyp.teumteumeat.domains.goal.persistence.entity.Goal;
import im.swyp.teumteumeat.domains.user.persistence.entity.UserEntity;

import java.time.LocalDate;
import java.util.List;

public class GoalMapper {
    public static Goal toGoal(
            UserEntity user,
            GoalRequest request
    ) {
        return Goal.builder()
                .user(user)
                .type(request.type())
                .endDate(request.endDate())
                .build();
    }

    public static GoalResponse fromGoal(Goal goal) {
        return GoalResponse.builder()
                .goalId(goal.getId())
                .type(goal.getType())
                .startDate(LocalDate.from(goal.getCreatedDate()))
                .endDate(goal.getEndDate())
                .build();
    }

    public static GoalListResponse toGoalListResponse(List<GoalResponse> goals) {
        return GoalListResponse.toGoalListResponse(goals);
    }
}
