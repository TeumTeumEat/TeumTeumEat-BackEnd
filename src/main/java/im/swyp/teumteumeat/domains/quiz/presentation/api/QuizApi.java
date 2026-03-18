package im.swyp.teumteumeat.domains.quiz.presentation.api;

import im.swyp.teumteumeat.domains.quiz.application.dto.response.QuizListResponse;
import im.swyp.teumteumeat.global.annotation.swagger.ApiResponseExplanations;
import im.swyp.teumteumeat.global.annotation.swagger.ApiSuccessResponseExplanation;
import im.swyp.teumteumeat.global.common.ApiResponse;
import im.swyp.teumteumeat.global.security.dto.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "Quiz", description = "퀴즈 API")
public interface QuizApi {

        @Operation(summary = "해당 카테고리 자료의 전체 퀴즈 목록 조회 (Goal 기반 필터링 포함 가능)")
        @ApiResponseExplanations(success = @ApiSuccessResponseExplanation(responseClass = QuizListResponse.class, description = "조회 성공"))
        ResponseEntity<ApiResponse<QuizListResponse>> getQuizzes(
                        @PathVariable Long categoryId,
                        @PathVariable Long documentId,
                        @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user);

        @Operation(summary = "PDF 문서에 대한 퀴즈 목록 조회")
        @ApiResponseExplanations(success = @ApiSuccessResponseExplanation(responseClass = QuizListResponse.class, description = "조회 성공"))
        ResponseEntity<ApiResponse<QuizListResponse>> getQuizzesOfDocument(
                        @PathVariable Long goalId,
                        @PathVariable Long documentId,
                        @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user);

        @Operation(summary = "퀴즈 단일 조회")
        @ApiResponseExplanations(success = @ApiSuccessResponseExplanation(responseClass = QuizListResponse.QuizDto.class, description = "조회 성공"))
        ResponseEntity<ApiResponse<QuizListResponse.QuizDto>> getQuiz(
                        @PathVariable Long categoryId,
                        @PathVariable Long documentId,
                        @PathVariable Long quizId,
                        @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user);

        @Operation(summary = "해당 카테고리 자료에 대한 퀴즈 생성", description = "사용자가 해당 카테고리 자료에 대한 퀴즈를 생성합니다. (결과는 비동기로 SSE를 통해 전달됨)")
        @ApiResponseExplanations(success = @ApiSuccessResponseExplanation(description = "생성 요청 성공 (결과는 비동기 SSE 알림)"))
        ResponseEntity<ApiResponse<Void>> createQuizzes(
                        @PathVariable Long categoryId,
                        @PathVariable Long documentId,
                        @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user);

        @Operation(summary = "해당 카테고리 자료에 대한 퀴즈 생성 SSE 구독", description = "비동기 퀴즈 생성 결과를 받기 위한 SSE 스트림에 연결합니다.")
        SseEmitter subscribeToCategoryQuiz(
                        @PathVariable Long categoryId,
                        @PathVariable Long documentId,
                        @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user,
                        @RequestHeader(value = "Last-Event-ID", required = false) String lastEventId,
                        HttpServletResponse response);

        @Operation(summary = "PDF 문서에 대한 퀴즈 생성", description = "문서 소유자가 퀴즈를 생성(재생성)할 수 있습니다. (결과는 비동기로 SSE를 통해 전달됨)")
        @ApiResponseExplanations(success = @ApiSuccessResponseExplanation(description = "생성 요청 성공 (결과는 비동기 SSE 알림)"))
        ResponseEntity<ApiResponse<Void>> createQuizzesForPdf(
                        @PathVariable Long goalId,
                        @PathVariable Long documentId,
                        @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user);

        @Operation(summary = "PDF 문서에 대한 퀴즈 생성 SSE 구독", description = "비동기 PDF 퀴즈 생성 결과를 받기 위한 SSE 스트림에 연결합니다.")
        org.springframework.web.servlet.mvc.method.annotation.SseEmitter subscribeToPdfQuiz(
                        @PathVariable Long goalId,
                        @PathVariable Long documentId,
                        @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user,
                        @org.springframework.web.bind.annotation.RequestHeader(value = "Last-Event-ID", required = false) String lastEventId,
                        jakarta.servlet.http.HttpServletResponse response);

        @Operation(summary = "퀴즈 삭제", description = "관리자(ADMIN)만 삭제할 수 있습니다.")
        @ApiResponseExplanations(success = @ApiSuccessResponseExplanation(description = "삭제 성공"))
        ResponseEntity<ApiResponse<Void>> deleteQuiz(
                        @PathVariable Long categoryId,
                        @PathVariable Long documentId,
                        @PathVariable Long quizId,
                        @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user);

}
