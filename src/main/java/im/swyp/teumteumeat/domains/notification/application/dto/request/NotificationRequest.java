package im.swyp.teumteumeat.domains.notification.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

public record NotificationRequest(
        @Schema(description = "알림 제목", example = "틈틈잇")
        String title,
        @Schema(description = "알림 본문", example = "지난주 주간 간식 리그 결과를 확인해 보세요🏆")
        String body,
        @Schema(description = "FCM data 페이로드 (type 값은 Notification 태그 설명 참고)", example = "{\"type\": \"LEAGUE_RESULT\"}")
        Map<String, String> data
) {
}
