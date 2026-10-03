package im.swyp.teumteumeat.domains.user.presentation.api.v2;

import im.swyp.teumteumeat.global.annotation.swagger.ApiResponseExplanations;
import im.swyp.teumteumeat.global.annotation.swagger.ApiSuccessResponseExplanation;
import im.swyp.teumteumeat.global.common.ApiResponse;
import im.swyp.teumteumeat.global.security.component.RefreshTokenCookieProvider;
import im.swyp.teumteumeat.global.security.dto.ReissueRequest;
import im.swyp.teumteumeat.global.security.token.TokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "User", description = "유저 API")
public interface UserApiV2 {

        @Operation(summary = "토큰 재발급",
                  description = """
                                refreshToken을 이용해 accessToken을 재발급합니다.
                                - refreshToken의 만료 기간이 일정 기준 이하이면 refreshToken이 함께 재발급됩니다. (프론트에서 교체 요망, 기준 미충족하는 경우 accessToken만 반환됨)
                                - 웹: `refresh_token` 쿠키로 요청하면 body는 무시되며, 재발급된 refreshToken은 쿠키로만 내려가고 응답 body의 refreshToken은 항상 null입니다.
                                  웹 소셜 로그인 직후 accessToken을 처음 획득할 때도 이 API를 호출합니다. (`credentials: 'include'` 필요)
                                """
        )
        @ApiResponseExplanations(success = @ApiSuccessResponseExplanation(responseClass = TokenResponse.class, description = "재발급 성공"))
        ResponseEntity<ApiResponse<TokenResponse>> tokenReissue(
                        @Parameter(hidden = true) @CookieValue(name = RefreshTokenCookieProvider.COOKIE_NAME, required = false) String cookieRefreshToken,
                        @RequestBody(required = false) ReissueRequest request,
                        HttpServletResponse response);
}
