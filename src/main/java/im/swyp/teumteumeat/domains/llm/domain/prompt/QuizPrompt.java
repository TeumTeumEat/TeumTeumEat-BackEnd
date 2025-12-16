package im.swyp.teumteumeat.domains.llm.domain.prompt;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum QuizPrompt {
    GENERATE_QUIZ("%s 카테고리에 해당하는 퀴즈들을 10문제 %s 형식(o,x 문제 또는 객관식 3지선다)으로 내줘. 난이도는 1,2,3 중 %d야");

    private final String template;
}
