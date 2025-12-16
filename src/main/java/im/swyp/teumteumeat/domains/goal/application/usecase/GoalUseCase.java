package im.swyp.teumteumeat.domains.goal.application.usecase;

import im.swyp.teumteumeat.domains.category.domain.service.CategoryService;
import im.swyp.teumteumeat.domains.category.persistence.entity.Category;
import im.swyp.teumteumeat.domains.goal.application.dto.request.GoalCreateRequest;
import im.swyp.teumteumeat.domains.goal.application.dto.request.GoalUpdateRequest;
import im.swyp.teumteumeat.domains.goal.application.dto.response.GoalListResponse;
import im.swyp.teumteumeat.domains.goal.application.dto.response.GoalResponse;
import im.swyp.teumteumeat.domains.goal.application.mapper.GoalMapper;
import im.swyp.teumteumeat.domains.goal.domain.service.GoalService;
import im.swyp.teumteumeat.domains.goal.persistence.entity.Goal;
import im.swyp.teumteumeat.domains.user.domain.service.UserService;
import im.swyp.teumteumeat.domains.user.persistence.entity.UserEntity;
import im.swyp.teumteumeat.global.annotation.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@UseCase
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GoalUseCase {

    private final GoalService goalService;
    private final CategoryService categoryService;
    private final UserService userService;

    public GoalListResponse getGoals(Long userId) {
        UserEntity user = userService.getUserById(userId);
        List<Goal> goals = goalService.getGoals(user);
        List<GoalResponse> responses = goals.stream().map(GoalMapper::fromGoal).toList();
        return GoalMapper.toGoalListResponse(responses);
    }

    @Transactional
    public void createGoal(Long userId, GoalCreateRequest request) {
        UserEntity user = userService.getUserById(userId);
        Category category = categoryService.getCategoryById(request.categoryId());
        Goal goal = GoalMapper.toGoal(user, request, category);
        goalService.createGoal(goal);
    }

    @Transactional
    public void updateGoal(Long userId, Long goalId, GoalUpdateRequest request) {
        Goal goal = goalService.getGoalById(goalId);
        goal.validateOwner(userId);

        goalService.updateGoal(goal, request);
    }

    @Transactional
    public void deleteGoal(Long userId, Long goalId) {
        Goal goal = goalService.getGoalById(goalId);
        goal.validateOwner(userId);

        goalService.deleteGoal(goalId);
    }
}
