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
import org.springframework.web.bind.annotation.PostMapping;
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

        @Operation(summary = "유저 퀴즈 생성 SSE 구독", description = "생성 비동기 처리 결과를 받기 위한 SSE 스트림에 연결합니다.")
        org.springframework.web.servlet.mvc.method.annotation.SseEmitter subscribe(
                        @RequestParam Long documentId,
                        @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user,
                        @org.springframework.web.bind.annotation.RequestHeader(value = "Last-Event-ID", required = false) String lastEventId,
                        jakarta.servlet.http.HttpServletResponse response);

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

        @Operation(summary = "퀴즈 세트 완료 처리", description = "퀴즈 세트를 완료하고 남은 풀이 횟수 차감 및 목표 달성도를 올립니다.")
        @ApiResponseExplanations(success = @ApiSuccessResponseExplanation(description = "처리 성공"))
        @PostMapping("/complete-set")
        ResponseEntity<ApiResponse<Void>> completeQuizSet(
                        @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user);

        @Operation(summary = "광고 시청 보상 획득", description = "광고 시청 후 퀴즈 풀이 가능 횟수를 1회 추가합니다.")
        @ApiResponseExplanations(success = @ApiSuccessResponseExplanation(description = "처리 성공"))
        @PostMapping("/ad-reward")
        ResponseEntity<ApiResponse<Void>> claimAdReward(
                        @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user);
}
