package im.swyp.teumteumeat.domains.quiz.presentation.api;

import im.swyp.teumteumeat.domains.quiz.application.dto.response.QuizListResponse;
import im.swyp.teumteumeat.global.annotation.swagger.ApiResponseExplanations;
import im.swyp.teumteumeat.global.annotation.swagger.ApiSuccessResponseExplanation;
import im.swyp.teumteumeat.global.common.ApiResponse;
import im.swyp.teumteumeat.global.security.annotation.LoginUser;
import im.swyp.teumteumeat.global.security.dto.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Quiz", description = "퀴즈 API")
public interface QuizApi {

        @Operation(summary = "전체 퀴즈 목록 조회",
                   description = """
                                 해당 카테고리 자료에 연결된 전체 퀴즈 목록을 조회합니다.
                                 
                                 **특징**
                                 - 필요에 따라 현재 진행 중인 Goal 기반으로 필터링이 가능합니다.
                                 """
        )
        @ApiResponseExplanations(success = @ApiSuccessResponseExplanation(responseClass = QuizListResponse.class, description = "조회 성공"))
        ResponseEntity<ApiResponse<QuizListResponse>> getQuizzes(
                        @PathVariable Long categoryId,
                        @PathVariable Long documentId);

        @Operation(summary = "PDF 문서에 대한 퀴즈 목록 조회",
                   description = """
                                 특정 PDF 문서에 연결된 전체 퀴즈 목록을 조회합니다.
                                 """
        )
        @ApiResponseExplanations(success = @ApiSuccessResponseExplanation(responseClass = QuizListResponse.class, description = "조회 성공"))
        ResponseEntity<ApiResponse<QuizListResponse>> getQuizzesOfDocument(
                        @PathVariable Long goalId,
                        @PathVariable Long documentId);

        @Operation(summary = "퀴즈 단일 조회",
                   description = """
                                 특정 퀴즈의 상세 정보를 단일 건으로 조회합니다.
                                 """
        )
        @ApiResponseExplanations(success = @ApiSuccessResponseExplanation(responseClass = QuizListResponse.QuizDto.class, description = "조회 성공"))
        ResponseEntity<ApiResponse<QuizListResponse.QuizDto>> getQuiz(
                        @PathVariable Long categoryId,
                        @PathVariable Long documentId,
                        @PathVariable Long quizId);

        @Operation(summary = "해당 카테고리 자료에 대한 퀴즈 생성",
                   description = """
                                 사용자가 지정된 카테고리 자료에 대한 퀴즈를 동기 방식으로 즉시 생성합니다.
                                 """
        )
        @ApiResponseExplanations(success = @ApiSuccessResponseExplanation(description = "생성 성공"))
        ResponseEntity<ApiResponse<Void>> createQuizzes(
                        @PathVariable Long categoryId,
                        @PathVariable Long documentId,
                        @Parameter(hidden = true) @LoginUser CustomUserDetails user);

        @Operation(summary = "PDF 문서에 대한 퀴즈 생성",
                   description = """
                                 해당 PDF 문서의 소유자가 문서 기반의 퀴즈를 동기로 생성(또는 재생성)합니다.
                                 """
        )
        @ApiResponseExplanations(success = @ApiSuccessResponseExplanation(description = "생성 성공"))
        ResponseEntity<ApiResponse<Void>> createQuizzesForPdf(
                        @PathVariable Long goalId,
                        @PathVariable Long documentId,
                        @Parameter(hidden = true) @LoginUser CustomUserDetails user);

        @Operation(summary = "퀴즈 삭제",
                   description = """
                                 지정된 퀴즈를 삭제합니다.
                                 
                                 **권한**
                                 - 관리자(ADMIN) 전용 API입니다.
                                 """
        )
        @ApiResponseExplanations(success = @ApiSuccessResponseExplanation(description = "삭제 성공"))
        ResponseEntity<ApiResponse<Void>> deleteQuiz(
                        @PathVariable Long categoryId,
                        @PathVariable Long documentId,
                        @PathVariable Long quizId);

}
