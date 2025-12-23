package im.swyp.teumteumeat.domains.llm.application.dto.response;

import im.swyp.teumteumeat.domains.quiz.domain.constant.QuizType;
import lombok.Builder;

import java.util.List;

@Builder
public record LLMResponse(
                List<Quiz> quizzes) {
        @Builder
        public record Quiz(
                        String question,
                        List<String> options,
                        String answer,
                        QuizType type,
                        String explanation) {
        }
}
