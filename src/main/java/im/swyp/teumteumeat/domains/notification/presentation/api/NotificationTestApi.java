package im.swyp.teumteumeat.domains.notification.presentation.api;

import im.swyp.teumteumeat.domains.notification.application.dto.request.NotificationRequest;
import im.swyp.teumteumeat.global.annotation.swagger.ApiResponseExplanations;
import im.swyp.teumteumeat.global.annotation.swagger.ApiSuccessResponseExplanation;
import im.swyp.teumteumeat.global.common.ApiResponse;
import im.swyp.teumteumeat.global.security.dto.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Notification(Test)", description = "푸쉬 알림 TEST API")
public interface NotificationTestApi {

    @Operation(
            summary = "푸쉬 알림 테스트",
            description = "관리자(ADMIN)만 전송할 수 있습니다."
    )
    @ApiResponseExplanations(
            success = @ApiSuccessResponseExplanation(
                    description = "전송 성공"
            )
    )
    ResponseEntity<ApiResponse<Void>> sendNotification(
            @RequestBody NotificationRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user
    );
}
