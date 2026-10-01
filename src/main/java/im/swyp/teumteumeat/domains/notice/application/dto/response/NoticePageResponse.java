package im.swyp.teumteumeat.domains.notice.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
public record NoticePageResponse(

        @Schema(description = "공지 목록") List<NoticeResponse> notices,

        @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0") int page,

        @Schema(description = "페이지 크기", example = "20") int size,

        @Schema(description = "전체 공지 수", example = "42") long totalElements,

        @Schema(description = "전체 페이지 수", example = "3") int totalPages
) {
}
