package im.swyp.teumteumeat.global.security.api;

import im.swyp.teumteumeat.global.annotation.swagger.ApiErrorResponseExplanation;
import im.swyp.teumteumeat.global.annotation.swagger.ApiResponseExplanations;
import im.swyp.teumteumeat.global.annotation.swagger.ApiSuccessResponseExplanation;
import im.swyp.teumteumeat.global.common.ApiResponse;
import im.swyp.teumteumeat.global.security.constant.AuthResponseCode;
import im.swyp.teumteumeat.global.security.constant.SocialProvider;
import im.swyp.teumteumeat.global.security.dto.LoginResponse;
import im.swyp.teumteumeat.global.security.dto.request.SignUpRequest;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

public interface AuthApi {

    @Operation(
            summary = "소셜로그인 회원가입(로그인)",
            description = "OAuth2 OIDC 회원가입(로그인) 요청"
    )
    @ApiResponseExplanations(
            success = @ApiSuccessResponseExplanation(
                    responseClass = LoginResponse.class,
                    description = "회원가입(로그인) 성공"
            ),
            errors = {
                    @ApiErrorResponseExplanation(exceptionCode = AuthResponseCode.class, name = "NEED_REGISTER"),
                    @ApiErrorResponseExplanation(exceptionCode = AuthResponseCode.class, name = "EXPIRED_JWT_TOKEN"),
                    @ApiErrorResponseExplanation(exceptionCode = AuthResponseCode.class, name = "INVALID_JWT_TOKEN")
            }
    )
    ResponseEntity<ApiResponse<LoginResponse>> oauthRegister(
            @RequestParam SocialProvider provider,
            @RequestBody @Valid SignUpRequest.Oidc request);
}
