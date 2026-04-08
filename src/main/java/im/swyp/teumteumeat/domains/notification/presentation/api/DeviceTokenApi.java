package im.swyp.teumteumeat.domains.notification.presentation.api;

import im.swyp.teumteumeat.domains.notification.application.dto.request.DeviceTokenRequest;
import im.swyp.teumteumeat.global.annotation.swagger.ApiResponseExplanations;
import im.swyp.teumteumeat.global.annotation.swagger.ApiSuccessResponseExplanation;
import im.swyp.teumteumeat.global.common.ApiResponse;
import im.swyp.teumteumeat.global.security.dto.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Notification", description = "푸쉬 알림 API")
public interface DeviceTokenApi {

    @Operation(
            summary = "디바이스 토큰 등록"
    )
    @ApiResponseExplanations(
            success = @ApiSuccessResponseExplanation(
                    description = "등록 성공"
            )
    )
    ResponseEntity<ApiResponse<Void>> registerDeviceToken(
            @RequestBody @Valid DeviceTokenRequest deviceTokenRequest,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user
    );

    @Operation(
            summary = "디바이스 토큰 삭제",
            description = "로그아웃 전 요청"
    )
    @ApiResponseExplanations(
            success = @ApiSuccessResponseExplanation(
                    description = "삭제 성공"
            )
    )
    ResponseEntity<ApiResponse<Void>> unregisterDeviceToken(
            @RequestBody @Valid DeviceTokenRequest deviceTokenRequest
    );
}
