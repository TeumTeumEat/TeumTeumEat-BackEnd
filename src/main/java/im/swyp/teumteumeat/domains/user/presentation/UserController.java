package im.swyp.teumteumeat.domains.user.presentation;

import im.swyp.teumteumeat.global.security.token.TokenResponse;
import im.swyp.teumteumeat.global.common.ApiResponse;
import im.swyp.teumteumeat.global.common.CommonResponseCode;
import im.swyp.teumteumeat.global.security.dto.ReissueRequest;
import im.swyp.teumteumeat.global.security.token.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final JwtProvider jwtProvider;

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