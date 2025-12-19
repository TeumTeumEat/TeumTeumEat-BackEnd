package im.swyp.teumteumeat.infra.s3.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.net.URL;

@Builder
public record PresignedUrlResponse(

        @Schema(description = "파일 업로드를 위한 URL", example = "https://teumteumeat.s3.ap-northeast-2.amazonaws.com/1/6cd45ae3-87a5_Lecture_1.pdf?X-Amz-Algorithm=AWS4-HMAC-SHA256...")
        URL presignedUrl,

        @Schema(description = "업로드 후 파일 접근 URL", example = "https://teumteumeat.s3.ap-northeast-2.amazonaws.com/1/6cd45ae3-87a5_Lecture_1.pdf")
        String fileUrl,

        @Schema(description = "파일 고유 Key", example = "1/6cd45ae3-87a5_Lecture_1.pdf")
        String key
) {
}
