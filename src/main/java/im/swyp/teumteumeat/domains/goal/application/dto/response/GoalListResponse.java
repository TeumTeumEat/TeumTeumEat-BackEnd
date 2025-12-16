package im.swyp.teumteumeat.domains.goal.application.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record GoalListResponse(

        List<GoalResponse> goalResponses
) {
    public static GoalListResponse toGoalListResponse(List<GoalResponse> goals) {
        return GoalListResponse.builder()
                .goalResponses(goals)
                .build();
    }
}
