package im.swyp.teumteumeat.domains.user.presentation;

import im.swyp.teumteumeat.domains.user.application.dto.request.CommuteInfoRequest;
import im.swyp.teumteumeat.domains.user.application.dto.response.CommuteInfoResponse;
import im.swyp.teumteumeat.domains.user.application.usecase.UserUseCase;
import im.swyp.teumteumeat.global.security.dto.CustomUserDetails;
import im.swyp.teumteumeat.global.security.token.TokenResponse;
import im.swyp.teumteumeat.global.common.ApiResponse;
import im.swyp.teumteumeat.global.common.CommonResponseCode;
import im.swyp.teumteumeat.global.security.dto.ReissueRequest;
import im.swyp.teumteumeat.global.security.token.JwtProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserUseCase userUseCase;
    private final JwtProvider jwtProvider;

    @GetMapping("/commute-info")
    public ResponseEntity<ApiResponse<CommuteInfoResponse>> getCommuteInfo(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        CommuteInfoResponse response = userUseCase.getCommuteInfo(user.getUserId());
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, response));
    }

    @PatchMapping("/commute-info")
    public ResponseEntity<ApiResponse<Void>> updateCommuteInfo(
            @RequestBody @Valid CommuteInfoRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        userUseCase.updateCommuteInfo(user.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK));
    }

    @GetMapping("/auth/success")
    public ResponseEntity<ApiResponse<TokenResponse>> loginSuccess(TokenResponse tokenResponse) {
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, tokenResponse));
    }

    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<String>> tokenReissue(@RequestBody ReissueRequest request) {
        String reissuedAccessToken = jwtProvider.reissueAccessToken(request.refreshToken());
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, reissuedAccessToken));
    }

    @GetMapping("/id")
    public ResponseEntity<ApiResponse<String>> mypage(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, userDetails.getUsername()));
    }

}