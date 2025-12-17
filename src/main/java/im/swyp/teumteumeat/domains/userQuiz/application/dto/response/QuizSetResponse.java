package im.swyp.teumteumeat.domains.userQuiz.application.dto.response;

import lombok.Builder;
import java.util.List;

@Builder
public record QuizSetResponse(
        Long quizId,
        String question,
        List<String> options,
        String type) {
}
