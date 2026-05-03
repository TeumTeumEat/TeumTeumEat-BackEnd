package im.swyp.teumteumeat.domains.user.presentation.api.v1;

import im.swyp.teumteumeat.domains.user.application.dto.request.CommuteInfoRequest;
import im.swyp.teumteumeat.domains.user.application.dto.request.NameRequest;
import im.swyp.teumteumeat.domains.user.application.dto.request.UserSettingsRequest;
import im.swyp.teumteumeat.domains.user.application.dto.response.*;
import im.swyp.teumteumeat.domains.goal.application.dto.response.GoalResponse;
import im.swyp.teumteumeat.global.annotation.swagger.ApiResponseExplanations;
import im.swyp.teumteumeat.global.annotation.swagger.ApiSuccessResponseExplanation;
import im.swyp.teumteumeat.global.common.ApiResponse;
import im.swyp.teumteumeat.global.security.annotation.LoginUser;
import im.swyp.teumteumeat.global.security.dto.CustomUserDetails;
import im.swyp.teumteumeat.global.security.dto.ReissueRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "User", description = "유저 API")
public interface UserApi {

        @Operation(summary = "유저 이름 조회", description = "요청 유저의 이름을 조회합니다.")
        @ApiResponseExplanations(success = @ApiSuccessResponseExplanation(responseClass = NameResponse.class, description = "조회 성공"))
        ResponseEntity<ApiResponse<NameResponse>> getName(
                        @Parameter(hidden = true) @LoginUser CustomUserDetails user);

        @Operation(summary = "유저 이름 수정", description = "요청 유저의 이름을 수정합니다." +
                        "<br>제약사항 : 1~10자, 공백 없는 한영숫자만 가능(^[a-zA-Z0-9가-힣]*$)")
        @ApiResponseExplanations(success = @ApiSuccessResponseExplanation(description = "수정 성공"))
        ResponseEntity<ApiResponse<Void>> updateName(
                        @RequestBody @Valid NameRequest request,
                        @Parameter(hidden = true) @LoginUser CustomUserDetails user);

        @Operation(summary = "출퇴근 정보 조회", description = "요청 유저의 출퇴근 정보를 조회합니다.")
        @ApiResponseExplanations(success = @ApiSuccessResponseExplanation(responseClass = CommuteInfoResponse.class, description = "조회 성공"))
        ResponseEntity<ApiResponse<CommuteInfoResponse>> getCommuteInfo(
                        @Parameter(hidden = true) @LoginUser CustomUserDetails user);

        @Operation(summary = "출퇴근 정보 수정(생성)", description = "요청 유저의 출퇴근 정보를 수정(초기 생성)합니다.")
        @ApiResponseExplanations(success = @ApiSuccessResponseExplanation(description = "수정 성공"))
        ResponseEntity<ApiResponse<Void>> updateCommuteInfo(
                        @RequestBody @Valid CommuteInfoRequest request,
                        @Parameter(hidden = true) @LoginUser CustomUserDetails user);

        @Operation(summary = "온보딩 완료 여부 조회", description = "요청 유저의 온보딩 완료 여부를 조회합니다. 이름 설정, 출퇴근 정보 등록, 목표 등록시에 완료됩니다.")
        @ApiResponseExplanations(success = @ApiSuccessResponseExplanation(responseClass = CompletedResponse.class, description = "조회 성공"))
        ResponseEntity<ApiResponse<CompletedResponse>> getOnboardingCompleted(
                        @Parameter(hidden = true) @LoginUser CustomUserDetails user);

        @Operation(summary = "유저 설정 상태 전체 조회", description = "요청 유저의 설정 상태 정보를 전체 조회합니다.")
        @ApiResponseExplanations(success = @ApiSuccessResponseExplanation(responseClass = UserSettingsResponse.class, description = "조회 성공"))
        ResponseEntity<ApiResponse<UserSettingsResponse>> getUserSettings(
                        @Parameter(hidden = true) @LoginUser CustomUserDetails user);

        @Operation(summary = "유저 설정 상태 업데이트", description = "요청 유저의 설정 상태 정보를 업데이트합니다.")
        @ApiResponseExplanations(success = @ApiSuccessResponseExplanation(description = "수정 성공"))
        ResponseEntity<ApiResponse<Void>> updateUserSettings(
                        @RequestBody @Valid UserSettingsRequest request,
                        @Parameter(hidden = true) @LoginUser CustomUserDetails user);

        @Operation(summary = "유저 계정 정보 조회", description = "소셜로그인 제공자/이메일 정보를 조회합니다.")
        @ApiResponseExplanations(success = @ApiSuccessResponseExplanation(responseClass = AccountInfoResponse.class, description = "조회 성공"))
        ResponseEntity<ApiResponse<AccountInfoResponse>> getAccountInfo(
                        @LoginUser CustomUserDetails user);

        @Operation(summary = "토큰 재발급", description = "refreshToken을 이용해 accessToken을 재발급합니다.")
        @ApiResponseExplanations(success = @ApiSuccessResponseExplanation(responseClass = String.class, description = "재발급 성공"))
        ResponseEntity<ApiResponse<String>> tokenReissue(
                        @RequestBody ReissueRequest request);

        @Operation(summary = "현재 목표 수정", description = "유저의 현재 진행 중인 목표를 설정합니다.")
        @ApiResponseExplanations(success = @ApiSuccessResponseExplanation(description = "수정 성공"))
        ResponseEntity<ApiResponse<Void>> updateCurrentGoal(
                        @RequestParam Long goalId,
                        @Parameter(hidden = true) @LoginUser CustomUserDetails user);

        @Operation(summary = "현재 목표 조회", description = "유저의 현재 진행 중인 목표를 조회합니다. (없을 경우 data: null)")
        @ApiResponseExplanations(success = @ApiSuccessResponseExplanation(responseClass = GoalResponse.class, description = "조회 성공"))
        ResponseEntity<ApiResponse<GoalResponse>> getCurrentGoal(
                        @Parameter(hidden = true) @LoginUser CustomUserDetails user);
}
