package im.swyp.teumteumeat.domains.llm.application.dto.request;

import im.swyp.teumteumeat.domains.llm.domain.constant.QuizType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LLMRequest {
    private String category;
    private QuizType type;
    private int level;
}