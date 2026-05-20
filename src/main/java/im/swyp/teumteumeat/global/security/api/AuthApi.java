package im.swyp.teumteumeat.global.security.api;

import im.swyp.teumteumeat.global.annotation.swagger.ApiErrorResponseExplanation;
import im.swyp.teumteumeat.global.annotation.swagger.ApiResponseExplanations;
import im.swyp.teumteumeat.global.annotation.swagger.ApiSuccessResponseExplanation;
import im.swyp.teumteumeat.global.common.ApiResponse;
import im.swyp.teumteumeat.global.security.annotation.LoginUser;
import im.swyp.teumteumeat.global.security.constant.AuthResponseCode;
import im.swyp.teumteumeat.global.security.constant.SocialProvider;
import im.swyp.teumteumeat.global.security.dto.CustomUserDetails;
import im.swyp.teumteumeat.global.security.dto.LoginResponse;
import im.swyp.teumteumeat.global.security.dto.request.SignUpRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

public interface AuthApi {

    @Operation(
            summary = "소셜로그인 회원가입(로그인)",
            description = """
                          - 로그인 클릭 시: termsAgreed: false로 요청
                            - 기존 유저: 로그인 처리 및 토큰 발급
                            - 신규 유저:
                              - AUTH-006(401) 예외 수신 시 약관 동의 팝업 노출
                              - 동의 후 termsAgreed: true로 재요청하여 회원가입 및 로그인 완료
                          """
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

    @Operation(
            summary = "로그아웃",
            description = "(Nullable) refreshToken 전송 시 서버에서 폐기합니다."
    )
    @ApiResponseExplanations(
            success = @ApiSuccessResponseExplanation(
                    description = "로그아웃 성공"
            )
    )
    ResponseEntity<ApiResponse<Void>> logOut(
            @RequestParam(required = false) String refreshToken,
            @Parameter(hidden = true) @LoginUser CustomUserDetails user
    );
}
