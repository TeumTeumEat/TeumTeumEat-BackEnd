package im.swyp.teumteumeat.domains.league.domain.constant;

import im.swyp.teumteumeat.global.common.BaseResponseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum LeagueResponseCode implements BaseResponseCode {
    INVALID_TIME_OFFSET(HttpStatus.BAD_REQUEST, "LEAGUE-001", "리그 시각 오프셋이 허용 범위를 벗어났습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
