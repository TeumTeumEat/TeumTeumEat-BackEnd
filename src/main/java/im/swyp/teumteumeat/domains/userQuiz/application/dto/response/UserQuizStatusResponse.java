package im.swyp.teumteumeat.domains.userQuiz.application.dto.response;

import lombok.Builder;

@Builder
public record UserQuizStatusResponse(
                boolean hasSolvedToday,
                boolean isFirstTime) {
}
