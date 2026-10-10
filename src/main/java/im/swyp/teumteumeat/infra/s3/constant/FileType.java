package im.swyp.teumteumeat.infra.s3.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FileType {
    USER_PDF("origin", "pdf", "application/pdf", 50L * 1024 * 1024), // 50MB
    ;

    // S3 key 경로: {env}/{pathPrefix}/{uuid}_{fileName}
    private final String pathPrefix;
    private final String extension;
    private final String contentType;
    private final long maxSize;
}
