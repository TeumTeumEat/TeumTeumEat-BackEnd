package im.swyp.teumteumeat.domains.goal.domain.constant;

import im.swyp.teumteumeat.global.common.BaseResponseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum GoalResponseCode implements BaseResponseCode {
    NOT_FOUND_GOAL(HttpStatus.NOT_FOUND, "GOAL-001", "존재하지 않는 목표입니다."),
    GOAL_COMPLETED(HttpStatus.BAD_REQUEST, "GOAL-003", "목표 학습 횟수를 완료하였습니다."),
    INVALID_PROMPT(HttpStatus.BAD_REQUEST, "GOAL-004", "적절하지 않은 프롬프트입니다. 학습 목표와 관련된 내용을 입력해주세요."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}