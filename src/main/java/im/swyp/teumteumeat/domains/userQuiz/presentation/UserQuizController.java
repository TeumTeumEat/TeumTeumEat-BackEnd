package im.swyp.teumteumeat.domains.userQuiz.presentation;

import im.swyp.teumteumeat.domains.userQuiz.application.dto.request.QuizSubmissionRequest;
import im.swyp.teumteumeat.domains.userQuiz.application.dto.response.QuizSetResponse;
import im.swyp.teumteumeat.domains.userQuiz.application.dto.response.QuizSubmissionResponse;
import im.swyp.teumteumeat.domains.userQuiz.application.usecase.UserQuizUseCase;
import im.swyp.teumteumeat.global.common.ApiResponse;
import im.swyp.teumteumeat.global.common.CommonResponseCode;
import im.swyp.teumteumeat.global.security.dto.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user-quizzes")
@RequiredArgsConstructor
public class UserQuizController {

    private final UserQuizUseCase userQuizUseCase;

    // 유저가 퀴즈를 푸는 기능
    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<QuizSubmissionResponse>> submitQuiz(
            @RequestBody @Valid QuizSubmissionRequest request,
            @AuthenticationPrincipal CustomUserDetails user) {
        QuizSubmissionResponse response = userQuizUseCase.submitQuiz(user.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, response));
    }

    // 퀴즈 10개 조회 (정답 미포함)
    @GetMapping
    public ResponseEntity<ApiResponse<List<QuizSetResponse>>> getQuizzes(
            @RequestParam Long documentId,
            @AuthenticationPrincipal CustomUserDetails user) {
        var response = userQuizUseCase.getQuizzesForSolving(documentId, user.getUserId());
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, response));
    }

    // 퀴즈 1개 조회 (정답 미포함)
    @GetMapping("/{quizId}")
    public ResponseEntity<ApiResponse<QuizSetResponse>> getQuiz(
            @PathVariable Long quizId) {
        var response = userQuizUseCase.getQuizForSolving(quizId);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, response));
    }
}
