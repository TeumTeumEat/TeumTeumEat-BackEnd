package im.swyp.teumteumeat.domains.notification.presentation.controller;

import im.swyp.teumteumeat.domains.notification.application.dto.request.DeviceTokenRequest;
import im.swyp.teumteumeat.domains.notification.application.usecase.DeviceTokenUseCase;
import im.swyp.teumteumeat.domains.notification.presentation.api.DeviceTokenApi;
import im.swyp.teumteumeat.global.common.ApiResponse;
import im.swyp.teumteumeat.global.common.CommonResponseCode;
import im.swyp.teumteumeat.global.security.annotation.LoginUser;
import im.swyp.teumteumeat.global.security.dto.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class DeviceTokenController implements DeviceTokenApi {

    private final DeviceTokenUseCase deviceTokenUseCase;

    @Override
    @PostMapping("/device-tokens")
    public ResponseEntity<ApiResponse<Void>> registerDeviceToken(
            @RequestBody @Valid DeviceTokenRequest request,
            @LoginUser CustomUserDetails user
    ) {
        deviceTokenUseCase.registerDeviceToken(user.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK));
    }

    @Override
    @DeleteMapping("/device-tokens")
    public ResponseEntity<ApiResponse<Void>> unregisterDeviceToken(
            @RequestBody @Valid DeviceTokenRequest request
    ) {
        deviceTokenUseCase.unregisterDeviceToken(request);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK));
    }
}
