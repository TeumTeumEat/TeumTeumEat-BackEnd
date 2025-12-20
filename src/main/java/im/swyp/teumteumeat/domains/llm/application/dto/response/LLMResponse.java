package im.swyp.teumteumeat.domains.llm.application.dto.response;

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
                        String type,
                        String explanation) {
        }
}
