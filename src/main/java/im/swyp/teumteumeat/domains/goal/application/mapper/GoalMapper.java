package im.swyp.teumteumeat.domains.goal.application.mapper;

import im.swyp.teumteumeat.domains.category.application.mapper.CategoryMapper;
import im.swyp.teumteumeat.domains.category.persistence.entity.Category;
import im.swyp.teumteumeat.domains.goal.application.dto.request.GoalCreateRequest;
import im.swyp.teumteumeat.domains.goal.application.dto.response.GoalListResponse;
import im.swyp.teumteumeat.domains.goal.application.dto.response.GoalResponse;
import im.swyp.teumteumeat.domains.goal.domain.util.DateParser;
import im.swyp.teumteumeat.domains.goal.persistence.entity.Goal;
import im.swyp.teumteumeat.domains.goal.domain.constant.GoalType;
import im.swyp.teumteumeat.domains.user.persistence.entity.UserEntity;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class GoalMapper {
    public static Goal toGoal(
            UserEntity user,
            GoalCreateRequest request,
            Category category,
            LocalDate startDate) {
        LocalDate endDate = DateParser.calculateEndDate(startDate, request.studyPeriod());

        return Goal.builder()
                .user(user)
                .type(request.type())
                .endDate(endDate)
                .difficulty(request.difficulty())
                .prompt(request.prompt())
                .category(category)
                .build();
    }

    public static GoalResponse fromGoal(Goal goal) {
        LocalDate startDate = LocalDate.from(goal.getCreatedDate());
        LocalDate endDate = goal.getEndDate();
        String studyPeriod = ChronoUnit.WEEKS.between(startDate, endDate) + "주";

        String fileName = null;
        Long documentId = null;
        if (goal.getType() == GoalType.DOCUMENT && !goal.getDocuments().isEmpty()) {
            fileName = goal.getDocuments().get(0).getFileName();
            documentId = goal.getDocuments().get(0).getId();
        }

        return GoalResponse.builder()
                .goalId(goal.getId())
                .type(goal.getType())
                .startDate(startDate)
                .endDate(endDate)
                .isExpired(endDate.isBefore(LocalDate.now()))
                .studyPeriod(studyPeriod)
                .difficulty(goal.getDifficulty())
                .prompt(goal.getPrompt())
                .prompt(goal.getPrompt())
                .fileName(fileName)
                .documentId(documentId)
                .category(goal.getCategory() != null
                        ? CategoryMapper.fromCategory(goal.getCategory())
                        : null)
                .build();
    }

    public static GoalListResponse toGoalListResponse(List<GoalResponse> goals) {
        return GoalListResponse.toGoalListResponse(goals);
    }
}
