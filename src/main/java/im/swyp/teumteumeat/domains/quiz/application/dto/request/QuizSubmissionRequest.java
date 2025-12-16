package im.swyp.teumteumeat.domains.quiz.application.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record QuizSubmissionRequest(
        @NotNull Long quizId,
        @NotNull String userAnswer) {
}
