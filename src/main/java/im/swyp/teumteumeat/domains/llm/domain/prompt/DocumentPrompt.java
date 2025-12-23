package im.swyp.teumteumeat.domains.llm.domain.prompt;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DocumentPrompt {
    GENERATE_DOCUMENT("%s 카테고리에 대한 핵심 개념이나 지식을 사용자가 공부할 수 있게 정리해서 설명해줘. " +
            "사용자가 학습할 수 있도록 한국어로 작성해줘. 이때 너의 응답 글자 수는 공백 포함 최대 500자야."),

    GENERATE_PDF_SUMMARY("다음 텍스트 내용을 500자 이내로 요약해줘.\n%s"),
    ;

    private final String template;
}
