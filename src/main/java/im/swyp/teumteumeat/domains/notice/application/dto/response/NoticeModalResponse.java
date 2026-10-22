package im.swyp.teumteumeat.domains.notice.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record NoticeModalResponse(

        @Schema(description = "공지 ID (오늘 하루 보지 않기 등 클라이언트 식별용)", example = "1") Long noticeId,

        @Schema(description = "제목 (모달 이미지 대체 텍스트, 전체화면 상단 제목 등)", example = "서비스 신규 오픈 안내") String title,

        @Schema(description = "모달 이미지 URL", example = "https://teumteumeat.s3.ap-northeast-2.amazonaws.com/prod/notice/1a2b3c4d-5e6f_modal.png")
        String modalImageUrl,

        @Schema(description = "모달 클릭 시 띄울 전체화면 데이터")
        NoticeDetailResponse detail
) {
}
