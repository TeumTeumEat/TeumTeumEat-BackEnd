package im.swyp.teumteumeat.domains.goal.presentation;

import im.swyp.teumteumeat.domains.goal.application.dto.request.GoalRequest;
import im.swyp.teumteumeat.domains.goal.application.dto.response.GoalListResponse;
import im.swyp.teumteumeat.domains.goal.application.usecase.GoalUseCase;
import im.swyp.teumteumeat.global.common.ApiResponse;
import im.swyp.teumteumeat.global.common.CommonResponseCode;
import im.swyp.teumteumeat.global.security.dto.CustomUserDetails;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/goals")
@RequiredArgsConstructor
public class GoalController {

    private final GoalUseCase goalUseCase;

    @GetMapping
    public ResponseEntity<ApiResponse<GoalListResponse>> getGoals(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        GoalListResponse response = goalUseCase.getGoals(user.getUserId());
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createGoal(
            @RequestBody @Valid GoalRequest goalRequest,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        goalUseCase.createGoal(user.getUserId(), goalRequest);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteGoal(
        @NotNull Long goalId,
        @AuthenticationPrincipal CustomUserDetails user
    ) {
        goalUseCase.deleteGoal(goalId, user.getUserId());
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK));
    }
}
