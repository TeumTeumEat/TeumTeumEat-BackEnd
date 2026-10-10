package im.swyp.teumteumeat.infra.s3.domain.service;

import im.swyp.teumteumeat.global.config.properties.S3Properties;
import im.swyp.teumteumeat.infra.s3.constant.FileType;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URL;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private static final long PRESIGNED_URL_EXPIRATION_MINUTES = 2;
    private static final String PATH_DELIMITER = "/";
    private static final String URL_PREFIX_DEFAULT = "https://%s.s3.%s.amazonaws.com/";

    private final S3Presigner s3Presigner;
    private final S3Properties s3Properties;
    private final Environment environment;

    public URL generatePresignedUrl(
            String key,
            Long contentLength,
            FileType fileType
    ) {
        return generatePresignedUrlInternal(key, contentLength, fileType);
    }

    public String createKey(String fileName, FileType fileType) {
        String env = getActiveProfile();
        return String.join(PATH_DELIMITER, env, fileType.getPathPrefix(), createUniqueFileName(fileName));
    }

    public String generateFileUrl(String key) {
        return urlPrefix() + key;
    }

    /* HELPER METHOD */
    // 파일 접근 URL 접두사: base-url(CDN 등)이 설정돼 있으면 사용, 없으면 S3 기본 URL
    private String urlPrefix() {
        String baseUrl = s3Properties.baseUrl();
        if (StringUtils.hasText(baseUrl)) {
            return baseUrl.endsWith(PATH_DELIMITER) ? baseUrl : baseUrl + PATH_DELIMITER;
        }
        return URL_PREFIX_DEFAULT.formatted(s3Properties.bucket(), s3Properties.region());
    }

    private String createUniqueFileName(final String originalFileName) {
        return String.format("%s_%s", UUID.randomUUID(), originalFileName);
    }

    private URL generatePresignedUrlInternal(String key, Long contentLength, FileType fileType) {
        PutObjectRequest.Builder builder = PutObjectRequest.builder()
                .bucket(s3Properties.bucket())
                .key(key)
                .contentType(fileType.getContentType());

        //todo V1 Deprecated시 contentLength을 필수값으로 요구
        if (contentLength != null) {
            builder.contentLength(contentLength);
        }

        PutObjectRequest objectRequest = builder.build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(PRESIGNED_URL_EXPIRATION_MINUTES))
                .putObjectRequest(objectRequest)
                .build();

        PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(presignRequest);
        return presigned.url();
    }

    private String getActiveProfile() {
        String[] profiles = environment.getActiveProfiles();
        if (profiles.length == 0) return "dev";

        String profile = profiles[0];
        // local은 dev로 매핑
        return profile.equals("prod") ? "prod" : "dev";
    }
}
