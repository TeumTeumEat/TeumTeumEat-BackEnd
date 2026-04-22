package im.swyp.teumteumeat.infra.file.constant;

import im.swyp.teumteumeat.global.common.BaseResponseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum FileResponseCode implements BaseResponseCode {
    NOT_SUPPORTED_EXTENSION(HttpStatus.BAD_REQUEST, "FILE-001", "지원되지 않는 파일 확장자입니다."),
    NOT_FOUND_S3_FILE(HttpStatus.NOT_FOUND, "FILE-002", "업로드된 문서가 아닙니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
