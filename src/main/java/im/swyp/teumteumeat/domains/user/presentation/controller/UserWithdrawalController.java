package im.swyp.teumteumeat.domains.user.presentation.controller;

import im.swyp.teumteumeat.domains.user.application.dto.request.UserWithdrawalRequest;
import im.swyp.teumteumeat.domains.user.application.usecase.UserWithdrawalUseCase;
import im.swyp.teumteumeat.global.common.ApiResponse;
import im.swyp.teumteumeat.global.common.CommonResponseCode;
import im.swyp.teumteumeat.global.security.dto.CustomUserDetails;
import im.swyp.teumteumeat.domains.user.presentation.api.UserWithdrawalApi;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserWithdrawalController implements UserWithdrawalApi {

    private final UserWithdrawalUseCase userWithdrawalUseCase;

    @Override
    @DeleteMapping("/withdrawal")
    public ResponseEntity<ApiResponse<Void>> withdraw(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody(required = false) UserWithdrawalRequest request) {
        if (request == null) {
            request = new UserWithdrawalRequest(null, null);
        }

        userWithdrawalUseCase.withdraw(userDetails.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK));
    }
}