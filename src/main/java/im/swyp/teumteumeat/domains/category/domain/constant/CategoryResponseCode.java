package im.swyp.teumteumeat.domains.category.domain.constant;

import im.swyp.teumteumeat.global.common.BaseResponseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CategoryResponseCode implements BaseResponseCode {
    NOT_FOUND_CATEGORY(HttpStatus.NOT_FOUND, "CATEGORY-001", "존재하지 않는 카테고리입니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}