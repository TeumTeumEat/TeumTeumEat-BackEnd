package im.swyp.teumteumeat.domains.user.domain.constant;

import im.swyp.teumteumeat.global.common.BaseResponseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserResponseCode implements BaseResponseCode {
    NOT_FOUND_USER(HttpStatus.NOT_FOUND, "USER-001", "존재하지 않는 유저입니다."),
    NOT_SET_COMMUTE_INFO(HttpStatus.NOT_FOUND, "USER-002", "아직 온보딩 정보가 설정되어 있지 않습니다.")
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}