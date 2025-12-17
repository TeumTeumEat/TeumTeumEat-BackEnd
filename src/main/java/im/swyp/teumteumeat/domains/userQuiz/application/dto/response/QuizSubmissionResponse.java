package im.swyp.teumteumeat.domains.userQuiz.application.dto.response;

import lombok.Builder;

@Builder
public record QuizSubmissionResponse(
                boolean isCorrect,
                String correctAnswer,
                String explanation) {
}
