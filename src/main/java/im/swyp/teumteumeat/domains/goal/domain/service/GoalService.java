package im.swyp.teumteumeat.domains.goal.domain.service;

import im.swyp.teumteumeat.domains.goal.application.dto.request.GoalUpdateRequest;
import im.swyp.teumteumeat.domains.goal.domain.constant.GoalResponseCode;
import im.swyp.teumteumeat.domains.goal.persistence.entity.Goal;
import im.swyp.teumteumeat.domains.goal.persistence.repository.GoalRepository;
import im.swyp.teumteumeat.domains.user.persistence.entity.UserEntity;
import im.swyp.teumteumeat.global.common.CommonResponseCode;
import im.swyp.teumteumeat.global.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GoalService {

    private final GoalRepository goalRepository;

    public Goal getGoalById(Long goalId) {
        return getOrThrow(goalId);
    }

    public List<Goal> getGoals(UserEntity user) {
        return goalRepository.findAllByUserId(user.getId());
    }

    public void createGoal(Goal goal) {
        goalRepository.save(goal);
    }

    public void updateGoal(Goal goal, GoalUpdateRequest request) {
        goal.updateGoal(
                request.endDate(),
                request.difficulty(),
                request.prompt());
    }

    public void deleteGoal(Long goalId) {
        goalRepository.deleteById(goalId);
    }

    public Goal findLatestGoal(Long userId, Long categoryId) {
        return goalRepository.findTopByUserIdAndCategoryIdOrderByCreatedDateDesc(userId, categoryId)
                .orElseThrow(() -> new BaseException(CommonResponseCode.NOT_FOUND));
    }

    public String getTopic(Long userId, Long categoryId) {
        Goal goal = goalRepository.findTopByUserIdAndCategoryIdOrderByCreatedDateDesc(userId, categoryId)
                .orElse(null);

        if (goal == null || goal.getPrompt() == null || goal.getPrompt().isEmpty()) {
            return "전반적인 내용";
        }
        return goal.getPrompt();
    }

    /* HELPER METHOD */
    private Goal getOrThrow(Long id) {
        return goalRepository.findById(id)
                .orElseThrow(() -> new BaseException(GoalResponseCode.NOT_FOUND_GOAL));
    }
}
