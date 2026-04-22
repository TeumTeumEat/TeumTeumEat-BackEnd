package im.swyp.teumteumeat.domains.common.llm.domain.constant;

import im.swyp.teumteumeat.global.common.BaseResponseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum LLMResponseCode implements BaseResponseCode {
    AI_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "LLM-001", "AI 콘텐츠 생성에 실패했습니다."),
    AI_QUOTA_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "LLM-002", "AI API 호출 한도를 초과했습니다."),
    AI_SERVER_ERROR(HttpStatus.BAD_GATEWAY, "LLM-003", "AI 서버가 응답하지 않습니다."),
    AI_INVALID_REQUEST(HttpStatus.BAD_REQUEST, "LLM-004", "잘못된 AI 요청입니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
