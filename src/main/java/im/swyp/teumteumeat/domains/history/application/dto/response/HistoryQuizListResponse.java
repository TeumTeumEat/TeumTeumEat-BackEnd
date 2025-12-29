package im.swyp.teumteumeat.domains.history.application.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record HistoryQuizListResponse(
                LocalDateTime createdAt,
                List<HistoryQuizDto> quizzes) {

        @Builder
        public record HistoryQuizDto(
                        Long quizId,
                        String question,
                        List<String> options,
                        String answer,
                        String type,
                        String explanation,
                        boolean isCorrect) {
        }
}
