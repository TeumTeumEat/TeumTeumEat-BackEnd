package im.swyp.teumteumeat.domains.quiz.presentation;

import im.swyp.teumteumeat.domains.quiz.application.dto.request.QuizSubmissionRequest;
import im.swyp.teumteumeat.domains.quiz.application.dto.response.QuizListResponse;
import im.swyp.teumteumeat.domains.quiz.application.dto.response.QuizSubmissionResponse;
import im.swyp.teumteumeat.domains.quiz.application.usecase.QuizUseCase;
import im.swyp.teumteumeat.domains.quiz.domain.service.QuizService;
import im.swyp.teumteumeat.global.common.ApiResponse;
import im.swyp.teumteumeat.global.common.CommonResponseCode;
import im.swyp.teumteumeat.global.security.dto.CustomUserDetails;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class QuizController {
    private final QuizUseCase quizUseCase;
    private final QuizService quizService;

    @GetMapping("/{categoryId}/documents/{documentId}/quizzes")
    public ResponseEntity<ApiResponse<QuizListResponse>> getQuizzes(
            @PathVariable Long categoryId,
            @PathVariable Long documentId,
            @AuthenticationPrincipal CustomUserDetails user) {
        QuizListResponse response = quizUseCase.getQuizzes(documentId);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, response));
    }

    @GetMapping("/{categoryId}/documents/{documentId}/quizzes/{quizId}")
    public ResponseEntity<ApiResponse<QuizListResponse.QuizDto>> getQuiz(
            @PathVariable Long categoryId,
            @PathVariable Long documentId,
            @PathVariable Long quizId,
            @AuthenticationPrincipal CustomUserDetails user) {
        QuizListResponse.QuizDto response = quizUseCase.getQuiz(quizId);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, response));
    }

    @PostMapping("/{categoryId}/documents/{documentId}/quizzes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> createQuizzes(
            @PathVariable Long categoryId,
            @PathVariable Long documentId,
            @AuthenticationPrincipal CustomUserDetails user) {
        quizUseCase.createQuizzesForDocument(documentId);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK));
    }

    @DeleteMapping("/{categoryId}/documents/{documentId}/quizzes/{quizId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @PathVariable Long categoryId,
            @PathVariable Long documentId,
            @PathVariable Long quizId,
            @AuthenticationPrincipal CustomUserDetails user) {
        quizUseCase.deleteQuiz(quizId);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK));
    }

    // 유저가 퀴즈를 푸는 기능
    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<QuizSubmissionResponse>> submitQuiz(
            @RequestBody @Valid QuizSubmissionRequest request,
            @AuthenticationPrincipal CustomUserDetails user) {
        QuizSubmissionResponse response = quizService.submitQuiz(user.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, response));
    }
}