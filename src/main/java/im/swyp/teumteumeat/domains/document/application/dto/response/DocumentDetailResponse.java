package im.swyp.teumteumeat.domains.document.application.dto.response;

import im.swyp.teumteumeat.domains.document.domain.constant.FileStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record DocumentDetailResponse(

        @Schema(description = "문서 ID", example = "1") Long documentId,

        @Schema(description = "요약글 ID (히스토리 조회용)", example = "1") Long documentSummaryId,

        @Schema(description = "파일명", example = "1강.pdf") String fileName,

        @Schema(description = "파일 Key", example = "1/sfwsw_1강.pdf") String fileKey,

        @Schema(description = "자료 제목", example = "Spring Framework 개요") String title,

        @Schema(description = "요약글", example = "이 문서는...") String summary,

        @Schema(description = "OCR 처리 상태", example = "COMPLETED") FileStatus status,

        @Schema(description = "오늘 퀴즈 풀이 여부", example = "true") boolean hasSolvedToday,

        @Schema(description = "최초 퀴즈 풀이 여부", example = "false") boolean isFirstTime,

        @Schema(description = "요약글 생성/수정 일시", example = "2024-01-01T12:00:00") LocalDateTime updatedAt) {
}
