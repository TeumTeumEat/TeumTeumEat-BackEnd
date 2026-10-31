package im.swyp.teumteumeat.domains.notice.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record NoticeDetailResponse(

        @Schema(description = "전체화면 이미지 URL", example = "https://teumteumeat.s3.ap-northeast-2.amazonaws.com/prod/notice/6cd45ae3-87a5_full.png")
        String fullScreenImageUrl,

        @Schema(description = "액션 버튼의 외부 URL (deepLink와 동시에 존재하지 않음)", example = "https://example.com/event", nullable = true)
        String externalLink,

        @Schema(description = "액션 버튼의 앱 내부 딥링크 (externalLink와 동시에 존재하지 않음)", example = "teumteumeat://quiz", nullable = true)
        String deepLink,

        @Schema(description = "액션 버튼 문구 (링크가 없으면 null)", example = "이벤트 참여하기", nullable = true)
        String actionButtonText
) {
}
