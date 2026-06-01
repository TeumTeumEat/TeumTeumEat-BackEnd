package im.swyp.teumteumeat.domains.user.presentation.controller.v1;

import im.swyp.teumteumeat.domains.goal.application.dto.response.GoalResponse;
import im.swyp.teumteumeat.domains.user.application.dto.request.CommuteInfoRequest;
import im.swyp.teumteumeat.domains.user.application.dto.request.NameRequest;
import im.swyp.teumteumeat.domains.user.application.dto.request.UserSettingsRequest;
import im.swyp.teumteumeat.domains.user.application.dto.response.NameResponse;
import im.swyp.teumteumeat.domains.user.application.dto.response.CommuteInfoResponse;
import im.swyp.teumteumeat.domains.user.application.dto.response.CompletedResponse;
import im.swyp.teumteumeat.domains.user.application.dto.response.UserSettingsResponse;
import im.swyp.teumteumeat.domains.user.application.dto.response.AccountInfoResponse;
import im.swyp.teumteumeat.domains.user.application.usecase.UserUseCase;
import im.swyp.teumteumeat.domains.user.presentation.api.v1.UserApi;
import im.swyp.teumteumeat.global.security.annotation.LoginUser;
import im.swyp.teumteumeat.global.security.dto.CustomUserDetails;
import im.swyp.teumteumeat.global.common.ApiResponse;
import im.swyp.teumteumeat.global.common.CommonResponseCode;
import im.swyp.teumteumeat.global.security.dto.ReissueRequest;
import im.swyp.teumteumeat.global.security.token.JwtProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController implements UserApi {

    private final UserUseCase userUseCase;
    private final JwtProvider jwtProvider;

    @Override
    @GetMapping("/name")
    public ResponseEntity<ApiResponse<NameResponse>> getName(
            @LoginUser CustomUserDetails user) {
        NameResponse response = userUseCase.getName(user.getUserId());
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, response));
    }

    @Override
    @PatchMapping("/name")
    public ResponseEntity<ApiResponse<Void>> updateName(
            @RequestBody @Valid NameRequest request,
            @LoginUser CustomUserDetails user) {
        userUseCase.updateName(user.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK));
    }

    @Override
    @GetMapping("/commute-info")
    public ResponseEntity<ApiResponse<CommuteInfoResponse>> getCommuteInfo(
            @LoginUser CustomUserDetails user) {
        CommuteInfoResponse response = userUseCase.getCommuteInfo(user.getUserId());
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, response));
    }

    @Override
    @PatchMapping("/commute-info")
    public ResponseEntity<ApiResponse<Void>> updateCommuteInfo(
            @RequestBody @Valid CommuteInfoRequest request,
            @LoginUser CustomUserDetails user) {
        userUseCase.updateCommuteInfo(user.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK));
    }

    @Override
    @GetMapping("/onboarding-completed")
    public ResponseEntity<ApiResponse<CompletedResponse>> getOnboardingCompleted(
            @LoginUser CustomUserDetails user) {
        CompletedResponse onboardingCompleted = userUseCase.isOnboardingCompleted(user.getUserId());
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, onboardingCompleted));
    }

    @Override
    @GetMapping("/settings")
    public ResponseEntity<ApiResponse<UserSettingsResponse>> getUserSettings(
            @LoginUser CustomUserDetails user) {
        UserSettingsResponse settingsResponse = userUseCase.getUserSettings(user.getUserId());
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, settingsResponse));
    }

    @Override
    @PatchMapping("/settings")
    public ResponseEntity<ApiResponse<Void>> updateUserSettings(
            @RequestBody @Valid UserSettingsRequest request,
            @LoginUser CustomUserDetails user) {
        userUseCase.updateUserSettings(user.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK));
    }

    @Override
    @GetMapping("/account-info")
    public ResponseEntity<ApiResponse<AccountInfoResponse>> getAccountInfo(
            @LoginUser CustomUserDetails user) {
        AccountInfoResponse response = userUseCase.getAccountInfo(user.getUserId());
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, response));
    }

    @Deprecated
    @Override
    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<String>> tokenReissue(@RequestBody @Valid ReissueRequest request) {
        String reissuedAccessToken = jwtProvider.reissueTokens(request.refreshToken()).accessToken();
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, reissuedAccessToken));
    }

    @Override
    @PatchMapping("/goal")
    public ResponseEntity<ApiResponse<Void>> updateCurrentGoal(
            @RequestParam Long goalId,
            @LoginUser CustomUserDetails user) {
        userUseCase.updateCurrentGoal(user.getUserId(), goalId);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK));
    }

    @Override
    @GetMapping("/goal")
    public ResponseEntity<ApiResponse<GoalResponse>> getCurrentGoal(
            @LoginUser CustomUserDetails user) {
        GoalResponse response = userUseCase.getCurrentGoal(user.getUserId());
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, response));
    }
}
