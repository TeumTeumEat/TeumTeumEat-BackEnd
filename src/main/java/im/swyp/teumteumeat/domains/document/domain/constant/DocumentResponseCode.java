package im.swyp.teumteumeat.domains.document.domain.constant;

import im.swyp.teumteumeat.global.common.BaseResponseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum DocumentResponseCode implements BaseResponseCode {
    NOT_FOUND_DOCUMENT(HttpStatus.NOT_FOUND, "DOCUMENT-001", "존재하지 않는 문서입니다."),
    DOCUMENT_NOT_READY(HttpStatus.BAD_REQUEST, "DOCUMENT-002", "문서 처리가 아직 완료되지 않았습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}