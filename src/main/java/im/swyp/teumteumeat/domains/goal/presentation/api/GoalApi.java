package im.swyp.teumteumeat.domains.goal.presentation.api;

import im.swyp.teumteumeat.domains.goal.application.dto.request.GoalCreateRequest;
import im.swyp.teumteumeat.domains.goal.application.dto.request.GoalUpdateRequest;
import im.swyp.teumteumeat.domains.goal.application.dto.response.GoalListResponse;
import im.swyp.teumteumeat.domains.goal.domain.constant.GoalResponseCode;
import im.swyp.teumteumeat.global.annotation.swagger.ApiErrorResponseExplanation;
import im.swyp.teumteumeat.global.annotation.swagger.ApiResponseExplanations;
import im.swyp.teumteumeat.global.annotation.swagger.ApiSuccessResponseExplanation;
import im.swyp.teumteumeat.global.common.ApiResponse;
import im.swyp.teumteumeat.global.common.CreatedResponse;
import im.swyp.teumteumeat.global.security.annotation.LoginUser;
import im.swyp.teumteumeat.global.security.dto.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Goal", description = "목표 API")
public interface GoalApi {

    @Operation(
            summary = "전체 목표 목록 조회",
            description = "요청 유저의 전체 목표 목록을 조회합니다."
    )
    @ApiResponseExplanations(
            success = @ApiSuccessResponseExplanation(
                    responseClass = GoalListResponse.class,
                    description = "조회 성공"
            )
    )
    ResponseEntity<ApiResponse<GoalListResponse>> getGoals(
            @Parameter(hidden = true) @LoginUser CustomUserDetails user
    );

    @Operation(
            summary = "목표 생성",
            description = "요청 유저의 목표를 생성합니다." +
                    "<br>type이 DOCUMENT인 경우 POST/PATCH에서 categoryId는 입력하지 않으며 category가 반환되지 않습니다." +
                    "<br>prompt는 30자 이하/미입력 가능하며 null인 경우 반환되지 않습니다." +
                    "<br>type이 DOCUMENT인 경우이고 /goals/{goalId}/documents를 통해 문서를 등록하지 않고 목표 생성 시에 문서를 등록하려고 하는 경우 fileName과 fileKey 2가지를 입력합니다.(s3 파일 업로드 선행)"
    )
    @ApiResponseExplanations(
            success = @ApiSuccessResponseExplanation(
                    responseClass = CreatedResponse.class,
                    description = "생성 성공"
            ),
            errors = {
                    @ApiErrorResponseExplanation(exceptionCode = GoalResponseCode.class, name = "INVALID_PROMPT")
            }
    )
    ResponseEntity<ApiResponse<CreatedResponse>> createGoal(
            @RequestBody @Valid GoalCreateRequest request,
            @Parameter(hidden = true) @LoginUser CustomUserDetails user
    );

    @Operation(
            summary = "목표 수정",
            description = "요청 유저의 목표를 수정합니다."
    )
    @ApiResponseExplanations(
            success = @ApiSuccessResponseExplanation(
                    description = "수정 성공"
            ),
            errors = {
                    @ApiErrorResponseExplanation(exceptionCode = GoalResponseCode.class, name = "INVALID_PROMPT")
            }
    )
    ResponseEntity<ApiResponse<Void>> updateGoal(
            @NotNull Long goalId,
            @RequestBody @Valid GoalUpdateRequest request,
            @Parameter(hidden = true) @LoginUser CustomUserDetails user
    );

    @Operation(
            summary = "목표 삭제",
            description = "요청 유저의 목표를 삭제합니다."
    )
    @ApiResponseExplanations(
            success = @ApiSuccessResponseExplanation(
                    description = "삭제 성공"
            ),
            errors = @ApiErrorResponseExplanation(exceptionCode = GoalResponseCode.class, name = "NOT_FOUND_GOAL")
    )
    ResponseEntity<ApiResponse<Void>> deleteGoal(
            @NotNull Long goalId,
            @Parameter(hidden = true) @LoginUser CustomUserDetails user
    );
}
