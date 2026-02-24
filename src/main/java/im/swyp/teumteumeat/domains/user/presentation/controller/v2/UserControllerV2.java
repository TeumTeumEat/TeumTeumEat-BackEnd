package im.swyp.teumteumeat.domains.user.presentation.controller.v2;

import im.swyp.teumteumeat.domains.user.presentation.api.v2.UserApiV2;
import im.swyp.teumteumeat.global.common.ApiResponse;
import im.swyp.teumteumeat.global.common.CommonResponseCode;
import im.swyp.teumteumeat.global.security.dto.ReissueRequest;
import im.swyp.teumteumeat.global.security.token.JwtProvider;
import im.swyp.teumteumeat.global.security.token.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2/users")
@RequiredArgsConstructor
public class UserControllerV2 implements UserApiV2 {

    private final JwtProvider jwtProvider;

    @Override
    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<TokenResponse>> tokenReissue(@RequestBody ReissueRequest request) {
        TokenResponse tokenResponse = jwtProvider.reissueTokens(request.refreshToken());
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, tokenResponse));
    }
}
