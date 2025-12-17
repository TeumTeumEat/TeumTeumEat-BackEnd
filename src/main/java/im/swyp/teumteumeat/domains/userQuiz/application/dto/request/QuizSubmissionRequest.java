package im.swyp.teumteumeat.domains.userQuiz.application.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record QuizSubmissionRequest(
        @NotNull @JsonProperty("quizId") Long quizId,
        @NotBlank @JsonProperty("userAnswer") String userAnswer) {
}
