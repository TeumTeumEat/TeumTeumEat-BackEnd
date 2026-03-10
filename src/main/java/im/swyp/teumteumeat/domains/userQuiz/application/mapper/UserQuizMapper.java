package im.swyp.teumteumeat.domains.userQuiz.application.mapper;

import im.swyp.teumteumeat.domains.goal.persistence.entity.Goal;
import im.swyp.teumteumeat.domains.user.domain.constant.Role;
import im.swyp.teumteumeat.domains.user.persistence.entity.UserEntity;
import im.swyp.teumteumeat.domains.userQuiz.application.dto.response.UserQuizStatusResponse;
import org.springframework.stereotype.Component;

@Component
public class UserQuizMapper {

    public UserQuizStatusResponse toStatusResponse(
            UserEntity userEntity,
            boolean hasSolvedToday,
            boolean hasSolvedEver,
            boolean hasGeneratedContent,
            boolean isQuizGuideSeen) {

        int availableQuizCount = userEntity.getAvailableQuizCount();
        int targetQuizSetCount = 0;
        int completedQuizSetCount = 0;

        Goal currentGoal = userEntity.getCurrentGoal();
        if (currentGoal != null) {
            targetQuizSetCount = (currentGoal.getTargetQuizSetCount() != null) ? currentGoal.getTargetQuizSetCount()
                    : 0;
            completedQuizSetCount = (currentGoal.getCompletedQuizSetCount() != null)
                    ? currentGoal.getCompletedQuizSetCount()
                    : 0;
        }

        if (userEntity.getRole() == Role.ADMIN) {
            hasSolvedToday = false;
            hasGeneratedContent = false;
            availableQuizCount = 999;
        }

        return UserQuizStatusResponse.builder()
                .hasSolvedToday(hasSolvedToday)
                .isFirstTime(!hasSolvedEver)
                .hasCreatedToday(hasGeneratedContent)
                .isQuizGuideSeen(isQuizGuideSeen)
                .availableQuizCount(availableQuizCount)
                .targetQuizSetCount(targetQuizSetCount)
                .completedQuizSetCount(completedQuizSetCount)
                .build();
    }
}
