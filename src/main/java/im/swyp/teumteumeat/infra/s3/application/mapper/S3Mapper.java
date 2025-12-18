package im.swyp.teumteumeat.infra.s3.application.mapper;

import im.swyp.teumteumeat.infra.s3.application.dto.PresignedUrlResponse;

import java.net.URL;

public class S3Mapper {

    public static PresignedUrlResponse toPresignedUrlResponse(URL presignedUrl, String fileUrl, String key) {
        return PresignedUrlResponse.builder()
                .presignedUrl(presignedUrl)
                .fileUrl(fileUrl)
                .key(key)
                .build();
    }
}
