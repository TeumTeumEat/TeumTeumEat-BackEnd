package im.swyp.teumteumeat.infra.s3.application.dto;

import im.swyp.teumteumeat.infra.s3.constant.FileType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PresignedUrlRequest(
        @NotBlank(message = "파일 이름은 비어있을 수 없습니다.")
        @Size(max = 100, message = "파일 이름은 100자 이하여야 합니다.")
        @Schema(description = "파일 이름", example = "1강.pdf")
        String fileName,

//      todo V1 Deprecated시 fileSize를 필수값으로 요구
//      @NotNull(message = "파일 크기는 비어있을 수 없습니다.")
//      @Positive
        @Schema(description = "파일 크기(byte)")
        Long fileSize,

        @Schema(description = "파일 종류 (미전송 시 USER_PDF)", defaultValue = "USER_PDF",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        FileType fileType
) {
    // 기존 클라이언트 호환: fileType 미전송 시 USER_PDF로 처리
    public PresignedUrlRequest {
        if (fileType == null) {
            fileType = FileType.USER_PDF;
        }
    }
}
