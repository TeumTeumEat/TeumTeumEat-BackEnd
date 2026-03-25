package im.swyp.teumteumeat.domains.goal.domain.util;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 사용자가 입력한 prompt에 대한 1단계 규칙 기반 필터.
 * 명백한 장난/무력화 시도를 LLM 호출 없이 빠르게 차단한다.
 */
public class PromptValidator {

    private PromptValidator() {}

    /** 차단 키워드 목록 (한국어 + 영어 Prompt Injection 패턴) */
    private static final List<String> BLOCKED_KEYWORDS = List.of(
            // 지시 거부 계열
            "하지마", "하지 마", "금지", "하면안돼", "하면 안 돼", "하지말아", "생성하지",
            "만들지마", "만들지 마", "쓰지마", "쓰지 않", "넣지마", "넣지 마",
            "무시해", "무시하고", "잊어버려", "잊어",
            // Prompt Injection (영어)
            "ignore", "forget", "disregard", "override", "bypass",
            "do not", "don't", "stop", "never",
            // 무의미한 지시 패턴
            "프롬프트", "시스템", "system prompt", "act as", "pretend", "roleplay"
    );

    /** 스팸성 특수문자 반복 패턴 (동일 문자 5회 이상 연속) */
    private static final Pattern SPAM_PATTERN = Pattern.compile("(.)\\1{4,}");

    /**
     * @return true  → 차단 대상 (부적합)
     *         false → 통과 (적합 가능성 있음, LLM 2단계 검증 필요)
     */
    public static boolean isBlocked(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            return false; // 빈 프롬프트는 "전반적인 내용"으로 처리되므로 검증 불필요
        }

        String lower = prompt.toLowerCase();

        for (String keyword : BLOCKED_KEYWORDS) {
            if (lower.contains(keyword.toLowerCase())) {
                return true;
            }
        }

        if (SPAM_PATTERN.matcher(prompt).find()) {
            return true;
        }

        return false;
    }
}
