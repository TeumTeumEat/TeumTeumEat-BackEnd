package im.swyp.teumteumeat.domains.notice.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record NoticeUpdateRequest(

        @NotBlank(message = "제목은 비어있을 수 없습니다.")
        @Size(max = 100, message = "제목은 100자 이하여야 합니다.")
        @Schema(description = "제목", example = "서비스 점검 안내")
        String title,

        @NotBlank(message = "전체화면 이미지 key는 비어있을 수 없습니다.")
        @Size(max = 255, message = "전체화면 이미지 key는 255자 이하여야 합니다.")
        @Schema(description = "전체화면 이미지 S3 key (Presigned Url 발급 시 fileType=NOTICE_IMAGE)", example = "prod/notice/6cd45ae3-87a5_full.png")
        String fullScreenImageKey,

        @NotBlank(message = "모달 이미지 key는 비어있을 수 없습니다.")
        @Size(max = 255, message = "모달 이미지 key는 255자 이하여야 합니다.")
        @Schema(description = "모달 이미지 S3 key (Presigned Url 발급 시 fileType=NOTICE_IMAGE)", example = "prod/notice/1a2b3c4d-5e6f_modal.png")
        String modalImageKey,

        @Size(max = 500, message = "외부 URL은 500자 이하여야 합니다.")
        @Schema(description = "액션 버튼의 외부 URL (deepLink와 동시 지정 불가, 둘 다 없으면 버튼 미노출)",
                example = "https://example.com/event", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String externalLink,

        @Size(max = 500, message = "딥링크는 500자 이하여야 합니다.")
        @Schema(description = "액션 버튼의 앱 내부 딥링크 (externalLink와 동시 지정 불가, 둘 다 없으면 버튼 미노출)",
                example = "teumteumeat://quiz", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String deepLink,

        @Size(max = 20, message = "액션 버튼 문구는 20자 이하여야 합니다.")
        @Schema(description = "액션 버튼 문구 (externalLink나 deepLink가 있으면 필수, 링크가 없으면 무시)",
                example = "이벤트 참여하기", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String actionButtonText,

        @NotNull(message = "팝업 시작 일시는 비어있을 수 없습니다.")
        @Schema(description = "메인 모달 노출 시작 일시", example = "2026-10-01T00:00:00")
        LocalDateTime popupStartAt,

        @NotNull(message = "팝업 종료 일시는 비어있을 수 없습니다.")
        @Schema(description = "메인 모달 노출 종료 일시 (시작 일시보다 뒤여야 함)", example = "2026-10-31T23:59:59")
        LocalDateTime popupEndAt
) implements NoticeContentRequest {
}
