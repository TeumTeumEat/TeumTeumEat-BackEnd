package im.swyp.teumteumeat.domains.goal.presentation.controller;

import im.swyp.teumteumeat.domains.goal.application.dto.request.GoalCreateRequest;
import im.swyp.teumteumeat.domains.goal.application.dto.request.GoalUpdateRequest;
import im.swyp.teumteumeat.domains.goal.application.dto.response.GoalListResponse;
import im.swyp.teumteumeat.domains.goal.application.usecase.GoalUseCase;
import im.swyp.teumteumeat.domains.goal.presentation.api.GoalApi;
import im.swyp.teumteumeat.global.common.ApiResponse;
import im.swyp.teumteumeat.global.common.CommonResponseCode;
import im.swyp.teumteumeat.global.common.CreatedResponse;
import im.swyp.teumteumeat.global.security.annotation.LoginUser;
import im.swyp.teumteumeat.global.security.dto.CustomUserDetails;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/goals")
@RequiredArgsConstructor
public class GoalController implements GoalApi {

    private final GoalUseCase goalUseCase;
    @Override
    @GetMapping
    public ResponseEntity<ApiResponse<GoalListResponse>> getGoals(
            @LoginUser CustomUserDetails user
    ) {
        GoalListResponse response = goalUseCase.getGoals(user.getUserId());
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, response));
    }

    @Override
    @PostMapping
    public ResponseEntity<ApiResponse<CreatedResponse>> createGoal(
            @RequestBody @Valid GoalCreateRequest request,
            @LoginUser CustomUserDetails user
    ) {
        Long savedId = goalUseCase.createGoal(user.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, CreatedResponse.from(savedId)));
    }

    @Override
    @PatchMapping
    public ResponseEntity<ApiResponse<Void>> updateGoal(
            @NotNull Long goalId,
            @RequestBody @Valid GoalUpdateRequest request,
            @LoginUser CustomUserDetails user
    ) {
        goalUseCase.updateGoal(user.getUserId(), goalId, request);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK));
    }

    @Override
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteGoal(
            @NotNull Long goalId,
            @LoginUser CustomUserDetails user
    ) {
        goalUseCase.deleteGoal(user.getUserId(), goalId);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK));
    }
}
