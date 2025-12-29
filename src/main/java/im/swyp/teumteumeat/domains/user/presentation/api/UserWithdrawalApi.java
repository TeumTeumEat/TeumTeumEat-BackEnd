package im.swyp.teumteumeat.domains.user.presentation.api;

import im.swyp.teumteumeat.global.common.ApiResponse;
import im.swyp.teumteumeat.global.security.dto.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Tag(name = "User Withdrawal", description = "회원 탈퇴 API")
public interface UserWithdrawalApi {

    @Operation(summary = "회원 탈퇴", description = "애플리케이션에서 회원을 탈퇴시키고 소셜 로그인 연결을 해제합니다.")
    ResponseEntity<ApiResponse<Void>> withdraw(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails);
}
