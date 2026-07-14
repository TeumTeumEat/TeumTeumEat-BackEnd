package im.swyp.teumteumeat.domains.llm.application.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import im.swyp.teumteumeat.domains.quiz.domain.constant.QuizType;
import lombok.Builder;

import java.util.List;

@Builder
public record LLMResponse(
                @JsonProperty(required = true) List<Quiz> quizzes) {
        @Builder
        public record Quiz(
                        @JsonProperty(required = true) String question,
                        @JsonProperty(required = true) List<String> options,
                        @JsonProperty(required = true) String answer,
                        @JsonProperty(required = true) QuizType type,
                        @JsonProperty(required = true) String explanation) {
        }
}
