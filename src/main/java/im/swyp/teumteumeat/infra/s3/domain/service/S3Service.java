package im.swyp.teumteumeat.infra.s3.domain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
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

    private final S3Presigner s3Presigner;

    @Value("${infra.aws.s3.bucket}")
    private String bucketName;

    @Value("${infra.aws.region.static}")
    private String region;

    public URL generatePresignedUrl(
            String key
    ) {
        return generatePresignedUrlInternal(key);
    }

    public String createKey(String fileName, Long userId) {
        return String.join(PATH_DELIMITER, userId.toString(), createUniqueFileName(fileName));
    }

    public String generateFileUrl(String key) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, key);
    }

    /* HELPER METHOD */
    private String createUniqueFileName(final String originalFileName) {
        return String.format("%s_%s", UUID.randomUUID(), originalFileName);
    }

    private URL generatePresignedUrlInternal(String key) {
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType("application/pdf")
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(PRESIGNED_URL_EXPIRATION_MINUTES))
                .putObjectRequest(objectRequest)
                .build();

        PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(presignRequest);
        return presigned.url();
    }
}
