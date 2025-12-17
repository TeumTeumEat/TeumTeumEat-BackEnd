package im.swyp.teumteumeat.domains.quiz.application.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record QuizListResponse(List<QuizDto> quizzes) {
    @Builder
    public record QuizDto(
            Long quizId,
            String question,
            List<String> options,
            String answer,
            String type,
            String explanation) {
    }
}
