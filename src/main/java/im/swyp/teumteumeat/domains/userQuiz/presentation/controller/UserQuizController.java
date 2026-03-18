package im.swyp.teumteumeat.domains.userQuiz.presentation.controller;

import im.swyp.teumteumeat.domains.userQuiz.application.dto.request.QuizSubmissionRequest;
import im.swyp.teumteumeat.domains.userQuiz.application.dto.response.QuizSetResponse;
import im.swyp.teumteumeat.domains.userQuiz.application.dto.response.QuizSubmissionResponse;
import im.swyp.teumteumeat.domains.userQuiz.application.dto.response.UserQuizStatusResponse;
import im.swyp.teumteumeat.domains.userQuiz.application.dto.response.QuizGuideResponse;
import im.swyp.teumteumeat.domains.userQuiz.application.usecase.UserQuizUseCase;
import im.swyp.teumteumeat.domains.userQuiz.presentation.api.UserQuizApi;
import im.swyp.teumteumeat.domains.goal.domain.constant.GoalType;
import im.swyp.teumteumeat.global.common.ApiResponse;
import im.swyp.teumteumeat.global.common.CommonResponseCode;
import im.swyp.teumteumeat.global.security.dto.CustomUserDetails;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user-quizzes")
@RequiredArgsConstructor
public class UserQuizController implements UserQuizApi {

    private final UserQuizUseCase userQuizUseCase;

    // 유저가 퀴즈를 푸는 기능
    @Override
    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<QuizSubmissionResponse>> submitQuiz(
            @RequestBody @Valid QuizSubmissionRequest request,
            @AuthenticationPrincipal CustomUserDetails user) {
        QuizSubmissionResponse response = userQuizUseCase.submitQuiz(user.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, response));
    }

    // 퀴즈 이동시간에 따른 개수만큼 조회 (정답 미포함)
    @Override
    @GetMapping
    public ResponseEntity<ApiResponse<List<QuizSetResponse>>> getQuizzes(
            @RequestParam Long documentId,
            @RequestParam(required = false, defaultValue = "CATEGORY") GoalType documentType,
            @AuthenticationPrincipal CustomUserDetails user) {
        List<QuizSetResponse> response = userQuizUseCase.getQuizzesForSolving(documentId, user.getUserId(),
                documentType);
        if (response.isEmpty()) {
            return ResponseEntity.accepted().body(ApiResponse.ofSuccess(CommonResponseCode.OK, response));
        }
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, response));
    }

    @Override
    @GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(
            @RequestParam Long documentId,
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestHeader(value = "Last-Event-ID", required = false) String lastEventId,
            HttpServletResponse response) {
        return userQuizUseCase.subscribe(user.getUserId(), documentId, lastEventId);
    }

    // 퀴즈 1개 조회 (정답 미포함)
    @Override
    @GetMapping("/{quizId}")
    public ResponseEntity<ApiResponse<QuizSetResponse>> getQuiz(
            @PathVariable Long quizId,
            @AuthenticationPrincipal CustomUserDetails user) {
        QuizSetResponse response = userQuizUseCase.getQuizForSolving(quizId);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, response));
    }

    @Override
    @PostMapping("/guide")
    public ResponseEntity<ApiResponse<QuizGuideResponse>> completeQuizGuide(
            @AuthenticationPrincipal CustomUserDetails user) {
        userQuizUseCase.completeQuizGuide(user.getUserId());
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, new QuizGuideResponse(true)));
    }

    @Override
    @PostMapping("/complete-set")
    public ResponseEntity<ApiResponse<Void>> completeQuizSet(
            @AuthenticationPrincipal CustomUserDetails user) {
        userQuizUseCase.completeQuizSet(user.getUserId());
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK));
    }

    @Override
    @PostMapping("/ad-reward")
    public ResponseEntity<ApiResponse<Void>> claimAdReward(
            @AuthenticationPrincipal CustomUserDetails user) {
        userQuizUseCase.claimAdReward(user.getUserId());
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK));
    }

    @Override
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<UserQuizStatusResponse>> getStatus(
            @AuthenticationPrincipal CustomUserDetails user) {

        UserQuizStatusResponse response = userQuizUseCase.getUserQuizStatus(user.getUserId());

        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, response));
    }
}
