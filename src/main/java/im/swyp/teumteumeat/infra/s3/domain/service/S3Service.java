package im.swyp.teumteumeat.infra.s3.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URL;
import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private static final long PRESIGNED_URL_EXPIRATION_MINUTES = 2;
    private static final String PATH_DELIMITER = "/";
    private static final String PATH_PREFIX = "origin";

    private final S3Presigner s3Presigner;

    @Value("${infra.aws.s3.bucket}")
    private String bucketName;

    @Value("${infra.aws.region.static}")
    private String region;

    private final Environment environment;

    public URL generatePresignedUrl(
            String key,
            Long contentLength
    ) {
        return generatePresignedUrlInternal(key, contentLength);
    }

    public String createKey(String fileName) {
        String env = getActiveProfile();
        return String.join(PATH_DELIMITER, env, PATH_PREFIX, createUniqueFileName(fileName));
    }

    public String generateFileUrl(String key) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, key);
    }

    /* HELPER METHOD */
    private String createUniqueFileName(final String originalFileName) {
        return String.format("%s_%s", UUID.randomUUID(), originalFileName);
    }

    private URL generatePresignedUrlInternal(String key, Long contentLength) {
        PutObjectRequest.Builder builder = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType("application/pdf");

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
