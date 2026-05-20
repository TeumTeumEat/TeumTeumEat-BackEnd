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
