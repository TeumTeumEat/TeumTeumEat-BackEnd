package im.swyp.teumteumeat.domains.llm.application.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LLMRequest {
    private String category;
    private int level;
}