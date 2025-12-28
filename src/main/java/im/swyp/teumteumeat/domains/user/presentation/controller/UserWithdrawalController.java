package im.swyp.teumteumeat.domains.user.presentation.controller;

import im.swyp.teumteumeat.domains.user.application.dto.request.UserWithdrawalRequest;
import im.swyp.teumteumeat.domains.user.application.usecase.UserWithdrawalUseCase;
import im.swyp.teumteumeat.global.common.ApiResponse;
import im.swyp.teumteumeat.global.common.CommonResponseCode;
import im.swyp.teumteumeat.global.security.dto.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User Withdrawal", description = "회원 탈퇴 API")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserWithdrawalController {

    private final UserWithdrawalUseCase userWithdrawalUseCase;

    @Operation(summary = "회원 탈퇴", description = "애플리케이션에서 회원을 탈퇴시키고 소셜 로그인 연결을 해제합니다.")
    @DeleteMapping("/withdrawal")
    public ResponseEntity<ApiResponse<Void>> withdraw(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody(required = false) UserWithdrawalRequest request) {
        if (request == null) {
            request = new UserWithdrawalRequest(null, null);
        }

        userWithdrawalUseCase.withdraw(userDetails.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, null));
    }
}
