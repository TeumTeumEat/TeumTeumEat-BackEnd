package im.swyp.teumteumeat.domains.notification.presentation.api;

import im.swyp.teumteumeat.domains.notification.application.dto.request.NotificationRequest;
import im.swyp.teumteumeat.global.annotation.swagger.ApiResponseExplanations;
import im.swyp.teumteumeat.global.annotation.swagger.ApiSuccessResponseExplanation;
import im.swyp.teumteumeat.global.common.ApiResponse;
import im.swyp.teumteumeat.global.security.annotation.LoginUser;
import im.swyp.teumteumeat.global.security.dto.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Notification")
public interface NotificationTestApi {

    @Operation(
            summary = "(ADMIN) 푸쉬 알림 테스트",
            description = """
                          - 유저 알림 설정이 켜져 있어야 합니다.
                          - 디바이스 토큰이 등록되어 있어야 합니다.

                          ### 푸쉬 알림 data.type 명세

                          모든 푸쉬 알림의 FCM `data.type`에 알림 종류가 담겨 전달됩니다.
                          알림 탭 시 화면 이동 및 GA 인입 경로 구분에 활용해 주세요. 이 API에서 type을 넣어 테스트 발송 가능합니다.

                          | type | 알림 | 발송 시점 (KST) |
                          |---|---|---|
                          | `DAILY_QUIZ` | 학습 알림 | 유저가 설정한 시간 (오늘 퀴즈를 푼 유저 제외) |
                          | `LEAGUE_DEADLINE` | 주간 간식 리그 마감 임박 | 일 20:00 |
                          | `LEAGUE_RESULT` | 지난주 주간 간식 리그 결과 | 월 08:00 (지난주 순위에 든 유저만) |
                          """
    )
    @ApiResponseExplanations(
            success = @ApiSuccessResponseExplanation(
                    description = "전송 성공"
            )
    )
    ResponseEntity<ApiResponse<Void>> sendNotification(
            @RequestBody NotificationRequest request,
            @Parameter(hidden = true) @LoginUser CustomUserDetails user
    );
}
