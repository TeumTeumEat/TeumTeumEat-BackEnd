package im.swyp.teumteumeat.infra.s3.application.dto;

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
        Long fileSize
) {
}