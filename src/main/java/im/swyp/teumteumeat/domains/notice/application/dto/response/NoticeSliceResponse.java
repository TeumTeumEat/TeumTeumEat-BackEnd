package im.swyp.teumteumeat.domains.notice.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
public record NoticeSliceResponse(

        @Schema(description = "공지 목록 (최신순)") List<NoticeResponse> notices,

        @Schema(description = "다음 공지 존재 여부, true이면 마지막 공지의 noticeId를 lastNoticeId로 다음 요청", example = "true") boolean hasNext
) {
}
