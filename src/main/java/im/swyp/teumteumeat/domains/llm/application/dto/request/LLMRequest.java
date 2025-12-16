package im.swyp.teumteumeat.domains.llm.application.dto.request;

import lombok.Builder;

@Builder
public record LLMRequest(
        String category,
        int level) {
}