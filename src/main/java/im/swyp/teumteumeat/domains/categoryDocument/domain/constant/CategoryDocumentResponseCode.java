package im.swyp.teumteumeat.domains.categoryDocument.domain.constant;

import im.swyp.teumteumeat.global.common.BaseResponseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CategoryDocumentResponseCode implements BaseResponseCode {
    NOT_FOUND_CATEGORY_DOCUMENT(HttpStatus.NOT_FOUND, "CATEGORY-DOCUMENT-001", "존재하지 않는 카테고리 문서(요약글)입니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}