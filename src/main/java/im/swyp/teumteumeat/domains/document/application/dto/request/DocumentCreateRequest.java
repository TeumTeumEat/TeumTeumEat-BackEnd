package im.swyp.teumteumeat.domains.document.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record DocumentCreateRequest(

                @Schema(description = "파일명", example = "1강.pdf") String fileName,

                @Schema(description = "파일 Key", example = "z121dfs_1강.pdf") String fileKey,

                @Schema(description = "난이도 (1:하, 2:중, 3:상)", example = "3", defaultValue = "3") @Min(1) @Max(3) Integer difficulty,

                @Schema(description = "퀴즈 주제 (선택)", example = "전반적인 내용") @Size(max = 30) String quizTopic) {
}
