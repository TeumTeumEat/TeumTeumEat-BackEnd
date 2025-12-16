package im.swyp.teumteumeat.domains.goal.domain.service;

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

    public Goal getGoal(Long goalId) {
        return getGoalById(goalId);
    }

    public List<Goal> getGoals(UserEntity user) {
        return goalRepository.findAllByUserId(user.getId());
    }

    public void createGoal(Goal goal) {
        goalRepository.save(goal);
    }

    public void updateGoal(Long goalId, Goal updateGoal) {
        Goal goal = getGoalById(goalId);
        goal.updateGoal(updateGoal);
    }

    public void deleteGoal(Long goalId) {
        goalRepository.deleteById(goalId);
    }

    public Goal getGoalById(Long id) {
        return getOrThrow(id);
    }

    /* HELPER METHOD */
    private Goal getOrThrow(Long id) {
        return goalRepository.findById(id)
                .orElseThrow(() -> new BaseException(CommonResponseCode.NOT_FOUND));
    }
}
