package im.swyp.teumteumeat.domains.league.presentation.api;

import im.swyp.teumteumeat.domains.league.application.dto.response.LeagueResponse;
import im.swyp.teumteumeat.domains.user.domain.constant.UserResponseCode;
import im.swyp.teumteumeat.global.annotation.swagger.ApiErrorResponseExplanation;
import im.swyp.teumteumeat.global.annotation.swagger.ApiResponseExplanations;
import im.swyp.teumteumeat.global.annotation.swagger.ApiSuccessResponseExplanation;
import im.swyp.teumteumeat.global.common.ApiResponse;
import im.swyp.teumteumeat.global.security.annotation.LoginUser;
import im.swyp.teumteumeat.global.security.dto.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "League", description = "주간 간식 리그 API")
public interface LeagueApi {

    @Operation(
            summary = "주간 간식 리그 랭킹 조회",
            description = """
                    이번 주(월 00:00 ~ 다음 주 월 00:00, KST) 스낵 수 기준 리그 랭킹을 조회합니다.

                    **집계 기준**
                    - 퀴즈 세트 1회 완료 = 스낵 1개
                    - 이번 주 스낵이 1개 이상인 유저만 랭킹에 포함됩니다.
                    - 동점일 경우 스트릭이 높은 유저, 그다음 가입일이 빠른 유저가 앞 순위입니다.

                    **응답**
                    - rankers: 상위 10명 (닉네임은 마스킹되어 내려갑니다)
                    - me: 내 순위 및 주간/오늘 스낵 수 (이번 주 스낵이 0개면 rank는 null)
                    - resetAt / remainingSeconds: 리그 리셋 시각 및 남은 시간
                    """
    )
    @ApiResponseExplanations(
            success = @ApiSuccessResponseExplanation(
                    responseClass = LeagueResponse.class,
                    description = "조회 성공"
            ),
            errors = {
                    @ApiErrorResponseExplanation(exceptionCode = UserResponseCode.class, name = "NOT_FOUND_USER")
            }
    )
    ResponseEntity<ApiResponse<LeagueResponse>> getLeague(
            @Parameter(hidden = true) @LoginUser CustomUserDetails user
    );
}
