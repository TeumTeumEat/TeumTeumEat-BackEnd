package im.swyp.teumteumeat.domains.notice.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record NoticeAdminResponse(

        @Schema(description = "공지 ID", example = "1") Long noticeId,

        @Schema(description = "제목", example = "서비스 신규 오픈 안내") String title,

        @Schema(description = "전체화면 이미지 S3 key (수정 요청에 그대로 전달)", example = "prod/notice/6cd45ae3-87a5_full.png")
        String fullScreenImageKey,

        @Schema(description = "전체화면 이미지 URL (미리보기용)", example = "https://teumteumeat.s3.ap-northeast-2.amazonaws.com/prod/notice/6cd45ae3-87a5_full.png")
        String fullScreenImageUrl,

        @Schema(description = "모달 이미지 S3 key (수정 요청에 그대로 전달)", example = "prod/notice/1a2b3c4d-5e6f_modal.png")
        String modalImageKey,

        @Schema(description = "모달 이미지 URL (미리보기용)", example = "https://teumteumeat.s3.ap-northeast-2.amazonaws.com/prod/notice/1a2b3c4d-5e6f_modal.png")
        String modalImageUrl,

        @Schema(description = "액션 버튼의 외부 URL", example = "https://example.com/event", nullable = true)
        String externalLink,

        @Schema(description = "액션 버튼의 앱 내부 딥링크", example = "teumteumeat://quiz", nullable = true)
        String deepLink,

        @Schema(description = "액션 버튼 문구", example = "이벤트 참여하기", nullable = true)
        String actionButtonText,

        @Schema(description = "메인 모달 노출 시작 일시", example = "2026-10-01T00:00:00")
        LocalDateTime popupStartAt,

        @Schema(description = "메인 모달 노출 종료 일시", example = "2026-10-31T23:59:59")
        LocalDateTime popupEndAt,

        @Schema(description = "작성일시", example = "2026-10-01T12:00:00") LocalDateTime createdDate
) {
}
