package im.swyp.teumteumeat.domains.user.presentation.controller.v2;

import im.swyp.teumteumeat.domains.user.presentation.api.v2.UserApiV2;
import im.swyp.teumteumeat.global.common.ApiResponse;
import im.swyp.teumteumeat.global.common.CommonResponseCode;
import im.swyp.teumteumeat.global.exception.BaseException;
import im.swyp.teumteumeat.global.security.component.OAuth2ResponseHandler;
import im.swyp.teumteumeat.global.security.constant.AuthResponseCode;
import im.swyp.teumteumeat.global.security.dto.ReissueRequest;
import im.swyp.teumteumeat.global.security.token.JwtProvider;
import im.swyp.teumteumeat.global.security.token.TokenResponse;
import im.swyp.teumteumeat.global.utils.CookieUtils;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/users")
@RequiredArgsConstructor
public class UserControllerV2 implements UserApiV2 {

    private final JwtProvider jwtProvider;
    private final CookieUtils cookieUtils;

    @Override
    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<TokenResponse>> tokenReissue(
            @CookieValue(name = "refresh_token", required = false) String cookieRefreshToken,
            @RequestBody(required = false) @Valid ReissueRequest request,
            HttpServletResponse response)
    {
        String refreshToken = (cookieRefreshToken != null) ? cookieRefreshToken : (request != null ? request.refreshToken() : null);
        if (refreshToken == null) {
            throw new BaseException(AuthResponseCode.INVALID_JWT_TOKEN);
        }
        TokenResponse result = jwtProvider.reissueTokens(refreshToken);
        if (cookieRefreshToken != null && result.refreshToken() != null) {
            int maxAge = (int) (jwtProvider.getJwtProperties().refreshToken().expirationTime() / 1000);
            cookieUtils.addCookie(response, OAuth2ResponseHandler.REFRESH_TOKEN_COOKIE_NAME, result.refreshToken(), maxAge);
        }
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, result));
    }
}
