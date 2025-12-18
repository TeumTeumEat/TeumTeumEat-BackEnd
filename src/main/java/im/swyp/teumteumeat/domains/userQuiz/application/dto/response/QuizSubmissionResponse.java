package im.swyp.teumteumeat.domains.userQuiz.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record QuizSubmissionResponse(

        @Schema(description = "퀴즈 제출 결과")
        boolean isCorrect,

        @Schema(description = "정답", example = "O/객관식")
        String correctAnswer,

        @Schema(description = "설명", example = "대한민국의 수도는 서울입니다.")
        String explanation
) {
}
