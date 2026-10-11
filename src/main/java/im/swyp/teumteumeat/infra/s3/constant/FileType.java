package im.swyp.teumteumeat.infra.s3.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;

import java.util.Set;

@Getter
@RequiredArgsConstructor
public enum FileType {
    USER_PDF("origin", Set.of(MediaType.APPLICATION_PDF), 50L * 1024 * 1024), // 50MB
    ;

    // S3 key 경로: {env}/{pathPrefix}/{uuid}_{fileName}
    private final String pathPrefix;
    // 허용 MIME 타입 (확장자 → MIME 매핑은 MediaTypeFactory 사용)
    private final Set<MediaType> allowedMediaTypes;
    private final long maxSize;
}
