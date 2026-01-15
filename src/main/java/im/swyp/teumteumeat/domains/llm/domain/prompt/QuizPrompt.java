package im.swyp.teumteumeat.domains.llm.domain.prompt;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum QuizPrompt {
        GENERATE_QUIZ("%s 카테고리에 해당하는 %d문제의 퀴즈들을 한국어로 내줘. " +
                        "이때, 퀴즈의 내용은 다음 자료를 기반으로 설정해야해. \n" +
                        "%s\n" +
                        "퀴즈의 형식은 'OX'와 'MCQ'를 섞어서 출제해줘. 이때 'OX'는 o,x 퀴즈, 'MCQ'는 답이 3개 중에 하나인 3지선다의 객관식 문제를 의미해. 반드시 퀴즈 형식에 맞는 문제를 생성해야해."
                        +
                        "퀴즈 질문 내에 정답이 직접적으로 들어가서는 절대 안되며, 'OX', 'MCQ' 형식을 명시하지마. 또한 질문(question) 텍스트 안에 객관식 보기(options) 내용을 나열하지 마."
                        +
                        "퀴즈 난이도는 %s야. (EASY: 쉬움, MEDIUM: 보통, HARD: 어려움)" +
                        "퀴즈 답은 반드시 퀴즈 형식('OX' 또는 'MCQ')에 맞게 설정해야해. 만약 'OX'면 답은 'O' 또는 'X'만 가능해." +
                        "추가적으로 다음 주제를 중점으로 퀴즈를 내줘: %s\n" +
                        "각 퀴즈의 질문(question)은 공백 포함 80자 이내로 간결하게 만들어줘.\n" +
                        "각 퀴즈에 대한 설명도 공백 포함 120자 이내로 만들어줘. \n"),

        GENERATE_DOCUMENT_QUIZ("다음 자료를 기반으로 %d문제의 퀴즈들을 한국어로 내줘. \n" +
                        "%s\n" +
                        "퀴즈의 형식은 'OX'와 'MCQ'를 섞어서 출제해줘. 이때 'OX'는 o,x 퀴즈, 'MCQ'는 답이 3개 중에 하나인 3지선다의 객관식 문제를 의미해. 반드시 퀴즈 형식에 맞는 문제를 생성해야해."
                        +
                        "퀴즈 질문 내에 정답이 직접적으로 들어가서는 절대 안되며, 'OX', 'MCQ' 형식을 명시하지마. 또한 질문(question) 텍스트 안에 객관식 보기(options) 내용을 나열하지 마."
                        +
                        "퀴즈 난이도는 %s야. (EASY: 쉬움, MEDIUM: 보통, HARD: 어려움)" +
                        "퀴즈 답은 반드시 퀴즈 형식('OX' 또는 'MCQ')에 맞게 설정해야해. 만약 'OX'면 답은 'O' 또는 'X'만 가능해." +
                        "추가적으로 다음 주제를 중점으로 퀴즈를 내줘: %s\n" +
                        "각 퀴즈의 질문(question)은 공백 포함 80자 이내로 간결하게 만들어줘.\n" +
                        "각 퀴즈에 대한 설명도 공백 포함 120자 이내로 만들어줘. \n");

        private final String template;
}
