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
import im.swyp.teumteumeat.global.security.dto.CustomUserDetails;
import im.swyp.teumteumeat.global.security.token.TokenResponse;
import im.swyp.teumteumeat.global.common.ApiResponse;
import im.swyp.teumteumeat.global.common.CommonResponseCode;
import im.swyp.teumteumeat.global.security.dto.ReissueRequest;
import im.swyp.teumteumeat.global.security.token.JwtProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
            @AuthenticationPrincipal CustomUserDetails user) {
        NameResponse response = userUseCase.getName(user.getUserId());
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, response));
    }

    @Override
    @PatchMapping("/name")
    public ResponseEntity<ApiResponse<Void>> updateName(
            @RequestBody @Valid NameRequest request,
            @AuthenticationPrincipal CustomUserDetails user) {
        userUseCase.updateName(user.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK));
    }

    @Override
    @GetMapping("/commute-info")
    public ResponseEntity<ApiResponse<CommuteInfoResponse>> getCommuteInfo(
            @AuthenticationPrincipal CustomUserDetails user) {
        CommuteInfoResponse response = userUseCase.getCommuteInfo(user.getUserId());
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, response));
    }

    @Override
    @PatchMapping("/commute-info")
    public ResponseEntity<ApiResponse<Void>> updateCommuteInfo(
            @RequestBody @Valid CommuteInfoRequest request,
            @AuthenticationPrincipal CustomUserDetails user) {
        userUseCase.updateCommuteInfo(user.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK));
    }

    @Override
    @GetMapping("/onboarding-completed")
    public ResponseEntity<ApiResponse<CompletedResponse>> getOnboardingCompleted(
            @AuthenticationPrincipal CustomUserDetails user) {
        CompletedResponse onboardingCompleted = userUseCase.isOnboardingCompleted(user.getUserId());
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, onboardingCompleted));
    }

    @Override
    @GetMapping("/settings")
    public ResponseEntity<ApiResponse<UserSettingsResponse>> getUserSettings(
            @AuthenticationPrincipal CustomUserDetails user) {
        UserSettingsResponse settingsResponse = userUseCase.getUserSettings(user.getUserId());
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, settingsResponse));
    }

    @Override
    @PatchMapping("/settings")
    public ResponseEntity<ApiResponse<Void>> updateUserSettings(
            @RequestBody @Valid UserSettingsRequest request,
            @AuthenticationPrincipal CustomUserDetails user) {
        userUseCase.updateUserSettings(user.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK));
    }

    @Override
    @GetMapping("/account-info")
    public ResponseEntity<ApiResponse<AccountInfoResponse>> getAccountInfo(
            @AuthenticationPrincipal CustomUserDetails user) {
        AccountInfoResponse response = userUseCase.getAccountInfo(user.getUserId());
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, response));
    }

    @Deprecated
    @GetMapping("/auth/success")
    public ResponseEntity<ApiResponse<TokenResponse>> loginSuccess(TokenResponse tokenResponse) {
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, tokenResponse));
    }

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
            @AuthenticationPrincipal CustomUserDetails user) {
        userUseCase.updateCurrentGoal(user.getUserId(), goalId);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK));
    }

    @Override
    @GetMapping("/goal")
    public ResponseEntity<ApiResponse<GoalResponse>> getCurrentGoal(
            @AuthenticationPrincipal CustomUserDetails user) {
        GoalResponse response = userUseCase.getCurrentGoal(user.getUserId());
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, response));
    }
}
