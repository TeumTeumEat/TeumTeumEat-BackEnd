package im.swyp.teumteumeat.domains.llm.application.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

@Builder
public record QuizValidationResponse(
        @JsonProperty(required = true) List<Result> results) {
    @Builder
    public record Result(
            @JsonProperty(required = true) int index,
            @JsonProperty(required = true) boolean isValid) {
    }
}
