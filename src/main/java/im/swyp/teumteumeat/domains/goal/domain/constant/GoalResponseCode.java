package im.swyp.teumteumeat.domains.goal.domain.constant;

import im.swyp.teumteumeat.global.common.BaseResponseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum GoalResponseCode implements BaseResponseCode {
    NOT_FOUND_GOAL(HttpStatus.NOT_FOUND, "GOAL-001", "존재하지 않는 목표입니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}