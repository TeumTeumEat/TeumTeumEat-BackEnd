package im.swyp.teumteumeat.infra.file.application.mapper;


import im.swyp.teumteumeat.infra.file.application.dto.PresignedUrlResponse;

import java.net.URL;

public class FileMapper {

    public static PresignedUrlResponse toPresignedUrlResponse(URL presignedUrl, String key) {
        return PresignedUrlResponse.builder()
                .presignedUrl(presignedUrl)
                .key(key)
                .build();
    }
}
