package im.swyp.teumteumeat.domains.quiz.presentation.controller;

import im.swyp.teumteumeat.domains.quiz.application.dto.response.QuizListResponse;
import im.swyp.teumteumeat.domains.quiz.application.usecase.QuizUseCase;
import im.swyp.teumteumeat.domains.quiz.presentation.api.QuizApi;
import im.swyp.teumteumeat.global.common.ApiResponse;
import im.swyp.teumteumeat.global.common.CommonResponseCode;
import im.swyp.teumteumeat.global.security.dto.CustomUserDetails;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class QuizController implements QuizApi {

    private final QuizUseCase quizUseCase;

    // 해당 카테고리 자료의 모든 퀴즈 조회
    @Override
    @GetMapping("categories/{categoryId}/documents/{documentId}/quizzes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<QuizListResponse>> getQuizzes(
            @PathVariable Long categoryId,
            @PathVariable Long documentId,
            @AuthenticationPrincipal CustomUserDetails user) {
        QuizListResponse response = quizUseCase.getQuizzesByCategoryDocumentId(documentId);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, response));
    }

    // PDF 문서에 대한 퀴즈 목록 조회
    @Override
    @GetMapping("goals/{goalId}/document/{documentId}/quizzes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<QuizListResponse>> getQuizzesOfDocument(
            @PathVariable Long goalId,
            @PathVariable Long documentId,
            @AuthenticationPrincipal CustomUserDetails user) {
        QuizListResponse response = quizUseCase.getQuizzesByDocumentId(documentId);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, response));
    }

    // 해당 퀴즈 한 개 조회
    @Override
    @GetMapping("categories/{categoryId}/documents/{documentId}/quizzes/{quizId}")
    @PreAuthorize("hasRole('ADMIN')")
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
        quizUseCase.createQuizzesForDocument(documentId, user.getUserId());
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK));
    }

    @Override
    @PostMapping("categories/{categoryId}/documents/{documentId}/quizzes/stream")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> createQuizzesStream(
            @PathVariable Long categoryId,
            @PathVariable Long documentId,
            @AuthenticationPrincipal CustomUserDetails user) {
        quizUseCase.createQuizzesForDocumentAsync(documentId, user.getUserId());
        return ResponseEntity.accepted().body(ApiResponse.ofSuccess(CommonResponseCode.OK));
    }

    @Override
    @GetMapping(value = "categories/{categoryId}/documents/{documentId}/quizzes/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public SseEmitter subscribeToCategoryQuiz(
            @PathVariable Long categoryId,
            @PathVariable Long documentId,
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestHeader(value = "Last-Event-ID", required = false) String lastEventId,
            HttpServletResponse response) {
        return quizUseCase.subscribe(user.getUserId(), documentId, lastEventId);
    }

    // PDF 문서에 대한 퀴즈 생성
    @Override
    @PostMapping("goals/{goalId}/documents/{documentId}/quizzes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> createQuizzesForPdf(
            @PathVariable Long goalId,
            @PathVariable Long documentId,
            @AuthenticationPrincipal CustomUserDetails user) {

        quizUseCase.createQuizzesForPdfDocumentById(documentId, user.getUserId());
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK));
    }

    @Override
    @PostMapping("goals/{goalId}/documents/{documentId}/quizzes/stream")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> createQuizzesForPdfStream(
            @PathVariable Long goalId,
            @PathVariable Long documentId,
            @AuthenticationPrincipal CustomUserDetails user) {

        quizUseCase.createQuizzesForPdfDocumentByIdAsync(documentId, user.getUserId());
        return ResponseEntity.accepted().body(ApiResponse.ofSuccess(CommonResponseCode.OK));
    }

    @Override
    @GetMapping(value = "goals/{goalId}/documents/{documentId}/quizzes/sse", produces = org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public org.springframework.web.servlet.mvc.method.annotation.SseEmitter subscribeToPdfQuiz(
            @PathVariable Long goalId,
            @PathVariable Long documentId,
            @AuthenticationPrincipal CustomUserDetails user,
            @org.springframework.web.bind.annotation.RequestHeader(value = "Last-Event-ID", required = false) String lastEventId,
            jakarta.servlet.http.HttpServletResponse response) {
        return quizUseCase.subscribe(user.getUserId(), documentId, lastEventId);
    }

    // 해당 퀴즈 삭제
    @Override
    @DeleteMapping("categories/{categoryId}/documents/{documentId}/quizzes/{quizId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteQuiz(
            @PathVariable Long categoryId,
            @PathVariable Long documentId,
            @PathVariable Long quizId,
            @AuthenticationPrincipal CustomUserDetails user) {
        quizUseCase.deleteQuiz(quizId);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK));
    }
}