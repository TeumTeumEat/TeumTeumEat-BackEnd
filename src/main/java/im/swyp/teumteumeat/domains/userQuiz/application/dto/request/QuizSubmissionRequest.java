package im.swyp.teumteumeat.domains.userQuiz.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record QuizSubmissionRequest(
        @NotNull Long quizId,
        @NotBlank String userAnswer) {
}
