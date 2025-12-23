package im.swyp.teumteumeat.domains.quiz.domain.constant;

import im.swyp.teumteumeat.global.common.BaseResponseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum QuizResponseCode implements BaseResponseCode {
    NOT_FOUND_QUIZ(HttpStatus.NOT_FOUND, "QUIZ-001", "존재하지 않는 퀴즈입니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}