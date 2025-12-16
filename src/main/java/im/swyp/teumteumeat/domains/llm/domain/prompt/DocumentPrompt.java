package im.swyp.teumteumeat.domains.llm.domain.prompt;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DocumentPrompt {
    GENERATE_DOCUMENT("%s 카테고리에 대한 핵심 개념 10가지를 요약해서 설명해줘. " +
            "사용자가 학습할 수 있도록 쉽고 명확하게 한국어로 작성해줘.");

    private final String template;
}
