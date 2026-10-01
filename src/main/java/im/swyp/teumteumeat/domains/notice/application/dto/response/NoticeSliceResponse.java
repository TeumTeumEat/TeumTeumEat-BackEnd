package im.swyp.teumteumeat.domains.notice.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
public record NoticeSliceResponse(

        @Schema(description = "공지 목록") List<NoticeResponse> notices,

        @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0") int page,

        @Schema(description = "페이지 크기", example = "20") int size,

        @Schema(description = "다음 페이지 존재 여부", example = "true") boolean hasNext
) {
}
