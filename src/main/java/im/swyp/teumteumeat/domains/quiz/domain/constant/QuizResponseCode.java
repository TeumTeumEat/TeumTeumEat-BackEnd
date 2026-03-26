package im.swyp.teumteumeat.domains.quiz.domain.constant;

import im.swyp.teumteumeat.global.common.BaseResponseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum QuizResponseCode implements BaseResponseCode {
    NOT_FOUND_QUIZ(HttpStatus.NOT_FOUND, "QUIZ-001", "존재하지 않는 퀴즈입니다."),
    TODAY_QUOTA_EXCEEDED(HttpStatus.BAD_REQUEST, "QUIZ-002", "배정된 퀴즈 풀이 횟수를 모두 소진했습니다. 내일 다시 시도하거나 광고를 시청해 주세요."),
    UNSOLVED_QUIZ_EXISTS(HttpStatus.BAD_REQUEST, "QUIZ-003", "아직 풀지 않은 요약글 또는 퀴즈가 존재합니다."),
    DAILY_AD_REWARD_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "QUIZ-004", "하루 최대 광고 시청 횟수(10회)를 초과했습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}