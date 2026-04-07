package im.swyp.teumteumeat.domains.userQuiz.application.dto.response;

import lombok.Builder;

@Builder
public record UserQuizStatusResponse(
            boolean hasSolvedToday,
            boolean isFirstTime,
            boolean hasCreatedToday,
            boolean isQuizGuideSeen,
            int availableQuizCount,
            int dailyAdRewardCount,
            boolean canIssueCoupon,
            int targetQuizSetCount,
            int completedQuizSetCount,
            boolean isCompleted) {
}
