package im.swyp.teumteumeat.domains.userQuiz.application.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record QuizSubmissionRequest(

        @NotNull
        @JsonProperty("quizId")
        @Schema(description = "퀴즈 ID", example = "1")
        Long quizId,

        @NotBlank
        @JsonProperty("userAnswer")
        @Schema(description = "정답 요청", example = "O / Ioc는...")
        String userAnswer
) {
}
