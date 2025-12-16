package im.swyp.teumteumeat.domains.goal.application.mapper;

import im.swyp.teumteumeat.domains.category.application.mapper.CategoryMapper;
import im.swyp.teumteumeat.domains.category.persistence.entity.Category;
import im.swyp.teumteumeat.domains.goal.application.dto.request.GoalCreateRequest;
import im.swyp.teumteumeat.domains.goal.application.dto.response.GoalListResponse;
import im.swyp.teumteumeat.domains.goal.application.dto.response.GoalResponse;
import im.swyp.teumteumeat.domains.goal.persistence.entity.Goal;
import im.swyp.teumteumeat.domains.user.persistence.entity.UserEntity;

import java.time.LocalDate;
import java.util.List;

public class GoalMapper {
    public static Goal toGoal(
            UserEntity user,
            GoalCreateRequest request,
            Category category
    ) {
        return Goal.builder()
                .user(user)
                .type(request.type())
                .endDate(request.endDate())
                .category(category)
                .build();
    }

    public static GoalResponse fromGoal(Goal goal) {
        return GoalResponse.builder()
                .goalId(goal.getId())
                .type(goal.getType())
                .startDate(LocalDate.from(goal.getCreatedDate()))
                .endDate(goal.getEndDate())
                .category(goal.getCategory() != null
                        ? CategoryMapper.fromCategory(goal.getCategory())
                        : null)
                .build();
    }

    public static GoalListResponse toGoalListResponse(List<GoalResponse> goals) {
        return GoalListResponse.toGoalListResponse(goals);
    }
}
