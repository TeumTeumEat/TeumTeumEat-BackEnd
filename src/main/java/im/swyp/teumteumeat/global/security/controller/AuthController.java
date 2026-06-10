package im.swyp.teumteumeat.global.security.controller;

import im.swyp.teumteumeat.global.common.ApiResponse;
import im.swyp.teumteumeat.global.common.CommonResponseCode;
import im.swyp.teumteumeat.global.security.annotation.LoginUser;
import im.swyp.teumteumeat.global.security.api.AuthApi;
import im.swyp.teumteumeat.global.security.component.OAuth2ResponseHandler;
import im.swyp.teumteumeat.global.security.constant.SocialProvider;
import im.swyp.teumteumeat.global.security.dto.CustomUserDetails;
import im.swyp.teumteumeat.global.security.dto.LoginResponse;
import im.swyp.teumteumeat.global.security.dto.request.SignUpRequest;
import im.swyp.teumteumeat.global.security.usecase.OAuth2UseCase;
import im.swyp.teumteumeat.global.utils.CookieUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "인증 관련 API")
public class AuthController implements AuthApi {

    private final OAuth2UseCase oAuth2UseCase;
    private final CookieUtils cookieUtils;

    @Override
    @PostMapping("/oauth/register")
    @PreAuthorize("isAnonymous()")
    public ResponseEntity<ApiResponse<LoginResponse>> oauthRegister(
            @RequestParam SocialProvider provider,
            @RequestBody @Valid SignUpRequest.Oidc request) {
        LoginResponse response = oAuth2UseCase.signUp(provider, request);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, response));
    }

    @Override
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logOut(
            @CookieValue(name = "refresh_token", required = false) String cookieRefreshToken,
            @RequestParam(required = false) String refreshToken,
            @LoginUser CustomUserDetails user,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String token = (cookieRefreshToken != null) ? cookieRefreshToken : refreshToken;
        oAuth2UseCase.logOut(user.getUserId(), token);
        if (cookieRefreshToken != null) {
            cookieUtils.deleteCookie(request, response, OAuth2ResponseHandler.REFRESH_TOKEN_COOKIE_NAME);
        }
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK));
    }
}
