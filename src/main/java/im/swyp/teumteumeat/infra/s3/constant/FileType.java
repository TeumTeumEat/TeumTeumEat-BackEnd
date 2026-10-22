package im.swyp.teumteumeat.infra.s3.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;

import java.util.Set;

@Getter
@RequiredArgsConstructor
public enum FileType {
    USER_PDF("origin", Set.of(MediaType.APPLICATION_PDF), 50L * 1024 * 1024, false), // 50MB
    NOTICE_IMAGE("notice", Set.of(MediaType.IMAGE_PNG, MediaType.IMAGE_JPEG, MediaType.parseMediaType("image/webp")),
            10L * 1024 * 1024, true), // 10MB
    ;

    // S3 key 경로: {env}/{pathPrefix}/{uuid}_{fileName}
    private final String pathPrefix;
    // 허용 MIME 타입 (확장자 → MIME 매핑은 MediaTypeFactory 사용)
    private final Set<MediaType> allowedMediaTypes;
    private final long maxSize;
    // 관리자만 업로드 URL을 발급받을 수 있는지 여부
    private final boolean adminOnly;
}
