package im.swyp.teumteumeat.domains.user.domain.constant;

import im.swyp.teumteumeat.global.common.BaseResponseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserResponseCode implements BaseResponseCode {
    NOT_FOUND_USER(HttpStatus.NOT_FOUND, "USER-001", "존재하지 않는 유저입니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}