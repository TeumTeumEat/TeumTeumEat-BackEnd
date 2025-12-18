package im.swyp.teumteumeat.domains.quiz.presentation.controller;

import im.swyp.teumteumeat.domains.quiz.application.dto.response.QuizListResponse;
import im.swyp.teumteumeat.domains.quiz.application.usecase.QuizUseCase;
import im.swyp.teumteumeat.domains.quiz.presentation.api.QuizApi;
import im.swyp.teumteumeat.global.common.ApiResponse;
import im.swyp.teumteumeat.global.common.CommonResponseCode;
import im.swyp.teumteumeat.global.security.dto.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class QuizController implements QuizApi {

    private final QuizUseCase quizUseCase;

    // 해당 카테고리 자료의 모든 퀴즈 조회
    @Override
    @GetMapping("categories/{categoryId}/documents/{documentId}/quizzes")
    public ResponseEntity<ApiResponse<QuizListResponse>> getQuizzes(
            @PathVariable Long categoryId,
            @PathVariable Long documentId,
            @AuthenticationPrincipal CustomUserDetails user) {
        QuizListResponse response = quizUseCase.getQuizzes(documentId);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, response));
    }

    // 해당 퀴즈 한 개 조회
    @Override
    @GetMapping("categories/{categoryId}/documents/{documentId}/quizzes/{quizId}")
    public ResponseEntity<ApiResponse<QuizListResponse.QuizDto>> getQuiz(
            @PathVariable Long categoryId,
            @PathVariable Long documentId,
            @PathVariable Long quizId,
            @AuthenticationPrincipal CustomUserDetails user) {
        QuizListResponse.QuizDto response = quizUseCase.getQuiz(quizId);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, response));
    }

    // 해당 카테고리 자료에 대한 퀴즈 생성
    @Override
    @PostMapping("categories/{categoryId}/documents/{documentId}/quizzes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> createQuizzes(
            @PathVariable Long categoryId,
            @PathVariable Long documentId,
            @AuthenticationPrincipal CustomUserDetails user) {
        quizUseCase.createQuizzesForDocument(documentId);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK));
    }

    // 해당 퀴즈 삭제
    @Override
    @DeleteMapping("categories/{categoryId}/documents/{documentId}/quizzes/{quizId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @PathVariable Long categoryId,
            @PathVariable Long documentId,
            @PathVariable Long quizId,
            @AuthenticationPrincipal CustomUserDetails user) {
        quizUseCase.deleteQuiz(quizId);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK));
    }
}