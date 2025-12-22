package im.swyp.teumteumeat.domains.history.application.dto.response;

import im.swyp.teumteumeat.domains.quiz.application.dto.response.QuizListResponse;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record HistoryDetailResponse(
                String title,
                String summary, // Full summary
                LocalDateTime createdAt,
                List<QuizListResponse.QuizDto> solvedQuizzes) {
}
