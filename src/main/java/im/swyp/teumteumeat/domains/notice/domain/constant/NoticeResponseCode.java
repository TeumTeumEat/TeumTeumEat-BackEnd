package im.swyp.teumteumeat.domains.notice.domain.constant;

import im.swyp.teumteumeat.global.common.BaseResponseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum NoticeResponseCode implements BaseResponseCode {
    NOT_FOUND_NOTICE(HttpStatus.NOT_FOUND, "NOTICE-001", "존재하지 않는 공지입니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
