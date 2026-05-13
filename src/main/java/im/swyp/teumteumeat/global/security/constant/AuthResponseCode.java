package im.swyp.teumteumeat.global.security.constant;

import im.swyp.teumteumeat.global.common.BaseResponseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthResponseCode implements BaseResponseCode {

    INVALID_HEADER(HttpStatus.BAD_REQUEST, "AUTH-001", "헤더가 올바르지 않습니다."),
    EXPIRED_JWT_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH-002", "토큰이 만료되었습니다."),
    INVALID_JWT_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH-003", "유효하지 않은 토큰입니다."),
    NOT_SUPPORTED_SOCIAL_PROVIDER(HttpStatus.BAD_REQUEST, "AUTH-004", "해당 소셜 로그인은 지원되지 않습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH-005", "인증 정보가 누락되었거나 유효하지 않습니다."),
    NEED_REGISTER(HttpStatus.UNAUTHORIZED, "AUTH-006", "회원가입이 필요한 유저입니다.(이용약관 미동의)"),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}