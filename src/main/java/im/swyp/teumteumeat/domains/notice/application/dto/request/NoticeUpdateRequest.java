package im.swyp.teumteumeat.domains.notice.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NoticeUpdateRequest(

        @NotBlank(message = "제목은 비어있을 수 없습니다.")
        @Size(max = 100, message = "제목은 100자 이하여야 합니다.")
        @Schema(description = "제목", example = "서비스 점검 안내")
        String title,

        @NotBlank(message = "내용은 비어있을 수 없습니다.")
        @Size(max = 5000, message = "내용은 5000자 이하여야 합니다.")
        @Schema(description = "내용", example = "안녕하세요. 틈틈잇...")
        String content
) {
}
