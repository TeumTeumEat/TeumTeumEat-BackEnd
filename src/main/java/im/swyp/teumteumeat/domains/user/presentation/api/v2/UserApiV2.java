package im.swyp.teumteumeat.domains.user.presentation.api.v2;

import im.swyp.teumteumeat.global.annotation.swagger.ApiResponseExplanations;
import im.swyp.teumteumeat.global.annotation.swagger.ApiSuccessResponseExplanation;
import im.swyp.teumteumeat.global.common.ApiResponse;
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

        @Operation(summary = "토큰 재발급", description = "refreshToken을 이용해 accessToken을 재발급합니다.<br>" +
                                                        "refreshToken의 만료 기간이 일정 기준 이하이면 refreshToken이 함께 재발급됩니다. (프론트에서 교체 요망, 기준 미충족하는 경우 accessToken만 반환됨)"
        )
        @ApiResponseExplanations(success = @ApiSuccessResponseExplanation(responseClass = TokenResponse.class, description = "재발급 성공"))
        ResponseEntity<ApiResponse<TokenResponse>> tokenReissue(
                        @Parameter(hidden = true) @CookieValue(name = "refresh_token", required = false) String cookieRefreshToken,
                        @RequestBody(required = false) ReissueRequest request,
                        HttpServletResponse response);
}
