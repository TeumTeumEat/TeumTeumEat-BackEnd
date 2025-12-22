package im.swyp.teumteumeat.domains.quiz.presentation.api;

import im.swyp.teumteumeat.domains.quiz.application.dto.response.QuizListResponse;
import im.swyp.teumteumeat.global.annotation.swagger.ApiResponseExplanations;
import im.swyp.teumteumeat.global.annotation.swagger.ApiSuccessResponseExplanation;
import im.swyp.teumteumeat.global.common.ApiResponse;
import im.swyp.teumteumeat.global.security.dto.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Tag(name = "Quiz", description = "퀴즈 API")
public interface QuizApi {

        @Operation(summary = "해당 카테고리 자료의 전체 퀴즈 목록 조회")
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

        @Operation(summary = "해당 카테고리 자료에 대한 퀴즈 생성", description = "관리자(ADMIN)만 생성할 수 있습니다.")
        @ApiResponseExplanations(success = @ApiSuccessResponseExplanation(description = "생성 성공"))
        ResponseEntity<ApiResponse<Void>> createQuizzes(
                        @PathVariable Long categoryId,
                        @PathVariable Long documentId,
                        @RequestParam(required = false, defaultValue = "3") @Min(1) @Max(3) int difficulty,
                        @RequestParam(required = false) String topic,
                        @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user);

        @Operation(summary = "PDF 문서에 대한 퀴즈 생성", description = "관리자(ADMIN)만 생성할 수 있습니다.")
        @ApiResponseExplanations(success = @ApiSuccessResponseExplanation(description = "생성 성공"))
        ResponseEntity<ApiResponse<Void>> createQuizzesForPdf(
                        @PathVariable Long goalId,
                        @PathVariable Long documentId,
                        @RequestParam(required = false, defaultValue = "3") @Min(1) @Max(3) int difficulty,
                        @RequestParam(required = false) String topic,
                        @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user);

        @Operation(summary = "퀴즈 삭제", description = "관리자(ADMIN)만 삭제할 수 있습니다.")
        @ApiResponseExplanations(success = @ApiSuccessResponseExplanation(description = "삭제 성공"))
        ResponseEntity<ApiResponse<Void>> deleteQuiz(
                        @PathVariable Long categoryId,
                        @PathVariable Long documentId,
                        @PathVariable Long quizId,
                        @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user);

}
