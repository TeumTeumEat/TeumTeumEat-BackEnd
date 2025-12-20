package im.swyp.teumteumeat.domains.llm.domain.prompt;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum QuizPrompt {
    GENERATE_QUIZ("%s 카테고리에 해당하는 10문제의 퀴즈들을 한국어로 내줘. " +
            "이때, 퀴즈의 내용은 다음 자료를 기반으로 설정해야해. \n" +
            "%s\n" +
            "퀴즈의 형식은 'OX'와 'MCQ'를 섞어서 출제해줘. 이때 'OX'는 o,x 퀴즈, 'MCQ'는 답이 3개 중에 하나인 3지선다의 객관식 문제를 의미해. " +
            "난이도는 1,2,3 중 %d야. 난이도는 1,2,3 각각이 하,중,상 단계에 해당해." +
            "각 퀴즈에 대한 설명도 공백 포함 200자 이내로 만들어줘. \n"),

    GENERATE_DOCUMENT_QUIZ("다음 자료를 기반으로 10문제의 퀴즈들을 한국어로 내줘. \n" +
            "%s\n" +
            "퀴즈의 형식은 'OX'와 'MCQ'를 섞어서 출제해줘. 이때 'OX'는 o,x 퀴즈, 'MCQ'는 답이 3개 중에 하나인 3지선다의 객관식 문제를 의미해. " +
            "난이도는 1,2,3 중 %d야. 난이도는 1,2,3 각각이 하,중,상 단계에 해당해." +
            "각 퀴즈에 대한 설명도 공백 포함 200자 이내로 만들어줘. \n");

    private final String template;
}
