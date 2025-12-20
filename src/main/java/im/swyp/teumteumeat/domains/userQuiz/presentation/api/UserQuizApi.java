package im.swyp.teumteumeat.domains.userQuiz.presentation.api;

import im.swyp.teumteumeat.domains.userQuiz.application.dto.request.QuizSubmissionRequest;
import im.swyp.teumteumeat.domains.userQuiz.application.dto.response.QuizSetResponse;
import im.swyp.teumteumeat.domains.userQuiz.application.dto.response.QuizSubmissionResponse;
import im.swyp.teumteumeat.domains.goal.domain.constant.GoalType;
import im.swyp.teumteumeat.global.annotation.swagger.ApiResponseExplanations;
import im.swyp.teumteumeat.global.annotation.swagger.ApiSuccessResponseExplanation;
import im.swyp.teumteumeat.global.common.ApiResponse;
import im.swyp.teumteumeat.global.security.dto.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "UserQuiz", description = "유저 퀴즈 API")
public interface UserQuizApi {

        @Operation(summary = "퀴즈 풀기 제출")
        @ApiResponseExplanations(success = @ApiSuccessResponseExplanation(responseClass = QuizSubmissionResponse.class, description = "제출 성공"))
        ResponseEntity<ApiResponse<QuizSubmissionResponse>> submitQuiz(
                        @RequestBody @Valid QuizSubmissionRequest request,
                        @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user);

        @Operation(summary = "퀴즈 10개 조회 (정답 미포함)")
        @ApiResponseExplanations(success = @ApiSuccessResponseExplanation(responseClass = QuizSetResponse.class, description = "조회 성공"))
        ResponseEntity<ApiResponse<List<QuizSetResponse>>> getQuizzes(
                        @RequestParam Long documentId,
                        @RequestParam(required = false, defaultValue = "CATEGORY") GoalType documentType,
                        @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user);

        @Operation(summary = "퀴즈 1개 조회 (정답 미포함)")
        @ApiResponseExplanations(success = @ApiSuccessResponseExplanation(description = "조회 성공"))
        ResponseEntity<ApiResponse<QuizSetResponse>> getQuiz(
                        @PathVariable Long quizId,
                        @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user);
}
