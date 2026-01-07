package im.swyp.teumteumeat.domains.userQuiz.presentation.api;

import im.swyp.teumteumeat.domains.userQuiz.application.dto.request.QuizSubmissionRequest;
import im.swyp.teumteumeat.domains.userQuiz.application.dto.response.QuizSetResponse;
import im.swyp.teumteumeat.domains.userQuiz.application.dto.response.QuizSubmissionResponse;
import im.swyp.teumteumeat.domains.userQuiz.application.dto.response.UserQuizStatusResponse;
import im.swyp.teumteumeat.domains.userQuiz.application.dto.response.QuizGuideResponse;
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

        @Operation(summary = "퀴즈 풀이 결과 제출", description = "사용자가 푼 퀴즈의 정답 여부를 제출하고 결과를 저장합니다.")
        @ApiResponseExplanations(success = @ApiSuccessResponseExplanation(responseClass = QuizSubmissionResponse.class, description = "제출 성공"))
        ResponseEntity<ApiResponse<QuizSubmissionResponse>> submitQuiz(
                        @RequestBody @Valid QuizSubmissionRequest request,
                        @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user);

        @Operation(summary = "퀴즈 사용자 이동시간 및 Goal(난이도, 프롬프트)에 해당하는 개수만큼 조회 (정답 미포함)")
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

        @Operation(summary = "퀴즈 안내 가이드 확인 처리", description = "사용자가 퀴즈 안내 가이드를 확인했음(다시 보지 않기 등)을 저장하고 현재 상태를 반환합니다.")
        @ApiResponseExplanations(success = @ApiSuccessResponseExplanation(responseClass = QuizGuideResponse.class, description = "처리 성공"))
        ResponseEntity<ApiResponse<QuizGuideResponse>> completeQuizGuide(
                        @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user);

        @Operation(summary = "유저 퀴즈/요약글 상태 조회", description = "오늘 퀴즈 풀이 여부 및 최초 풀이 여부, 오늘 요약글 생성 여부를 반환합니다. (홈/인트로 화면용)")
        @ApiResponseExplanations(success = @ApiSuccessResponseExplanation(responseClass = UserQuizStatusResponse.class, description = "조회 성공"))
        ResponseEntity<ApiResponse<UserQuizStatusResponse>> getStatus(
                        @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user);
}
