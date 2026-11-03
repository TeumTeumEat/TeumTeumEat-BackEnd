package im.swyp.teumteumeat.domains.notice.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record NoticeResponse(

        @Schema(description = "공지 ID", example = "1") Long noticeId,

        @Schema(description = "제목", example = "서비스 신규 오픈 안내") String title,

        @Schema(description = "작성일시", example = "2026-10-01T12:00:00") LocalDateTime createdDate,

        @Schema(description = "공지 클릭 시 띄울 전체화면 공지 (메인 모달의 detail과 동일)")
        NoticeDetailResponse detail
) {
}
