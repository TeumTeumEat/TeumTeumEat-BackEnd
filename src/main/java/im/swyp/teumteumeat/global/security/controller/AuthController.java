package im.swyp.teumteumeat.global.security.controller;

import im.swyp.teumteumeat.global.common.ApiResponse;
import im.swyp.teumteumeat.global.common.CommonResponseCode;
import im.swyp.teumteumeat.global.security.api.AuthApi;
import im.swyp.teumteumeat.global.security.constant.SocialProvider;
import im.swyp.teumteumeat.global.security.dto.CustomUserDetails;
import im.swyp.teumteumeat.global.security.dto.LoginResponse;
import im.swyp.teumteumeat.global.security.dto.request.SignUpRequest;
import im.swyp.teumteumeat.global.security.usecase.OAuth2UseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "인증 관련 API")
public class AuthController implements AuthApi {

    private final OAuth2UseCase oAuth2UseCase;

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
            @RequestParam(required = false) String refreshToken,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        oAuth2UseCase.logOut(user.getUserId(), refreshToken);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK));
    }
}
