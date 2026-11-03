package im.swyp.teumteumeat.domains.notice.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
public record NoticeModalListResponse(

        @Schema(description = "모달 공지 목록 (최신순, 없으면 빈 배열)") List<NoticeModalResponse> notices
) {
}
