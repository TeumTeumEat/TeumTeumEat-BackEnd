package im.swyp.teumteumeat.infra.s3.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.net.URL;

@Builder
public record PresignedUrlResponse(

        @Schema(description = "파일 업로드를 위한 URL")
        URL presignedUrl,

        @Schema(description = "업로드 후 파일 접근 URL")
        String fileUrl,

        @Schema(description = "파일 고유 Key")
        String key
) {
}
