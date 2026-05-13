package im.swyp.teumteumeat.domains.goal.domain.util;

import org.springframework.util.StringUtils;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 사용자가 입력한 prompt에 대한 규칙 기반 키워드 필터.
 * 명백한 장난/무력화 시도를 LLM 호출 없이 빠르게 차단한다.
 * 통과한 입력은 LLM 프롬프트 내 인라인 가이드라인으로 추가 처리된다.
 */
public class PromptValidator {

    private PromptValidator() {}

    /** 차단 키워드 목록 (한국어 + 영어 Prompt Injection 패턴) */
    private static final List<String> BLOCKED_KEYWORDS = List.of(
            // 명령/지시 무력화 - 학습 맥락에서 절대 안 쓰이는 패턴
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
     *         false → 통과 (LLM 프롬프트 인라인 가이드라인으로 처리)
     */
    public static boolean isBlocked(String prompt) {
        if (!StringUtils.hasText(prompt)) {
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
