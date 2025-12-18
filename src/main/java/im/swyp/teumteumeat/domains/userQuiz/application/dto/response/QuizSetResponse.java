package im.swyp.teumteumeat.domains.userQuiz.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import java.util.List;

@Builder
public record QuizSetResponse(

        @Schema(description = "퀴즈 ID", example = "1")
        Long quizId,

        @Schema(description = "질문", example = "대한민국의 수도는?")
        String question,

        @Schema(description = "퀴즈 문항")
        List<String> options,

        @Schema(description = "퀴즈 유형", example = "OX/MCQ")
        String type
) {
}
