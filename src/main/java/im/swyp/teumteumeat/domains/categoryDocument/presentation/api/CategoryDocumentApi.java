package im.swyp.teumteumeat.domains.categoryDocument.presentation.api;

import im.swyp.teumteumeat.domains.categoryDocument.application.dto.response.CategoryDocumentResponse;
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

@Tag(name = "CategoryDocument", description = "카테고리 자료(요약글) API")
public interface CategoryDocumentApi {

        @Operation(summary = "일일 카테고리 요약글 생성 (학습 시작)", description = "오늘 학습할 새로운 카테고리 요약글과 퀴즈를 생성합니다. (요청 접수 후 비동기로 생성되며 SSE로 결과가 전달됩니다.)")
        @ApiResponseExplanations(success = @ApiSuccessResponseExplanation(description = "생성 요청 접수 성공 (결과는 SSE로 알림)"))
        ResponseEntity<ApiResponse<Void>> generateDocument(
                        @PathVariable Long categoryId,
                        @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user);

        @Operation(summary = "카테고리 생성 SSE 구독", description = "생성 비동기 처리 결과를 받기 위한 SSE 스트림에 연결합니다.")
        SseEmitter subscribe(
                        @PathVariable Long categoryId,
                        @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user,
                        @RequestHeader(value = "Last-Event-ID", required = false) String lastEventId,
                        HttpServletResponse response);

        @Operation(summary = "일일 카테고리 요약글 단순 조회 (이어 읽기)", description = "유저가 최근에 발급받아 현재 진행 중인 카테고리 요약글을 횟수 차감 없이 그대로 다시 조회합니다.")
        @ApiResponseExplanations(success = @ApiSuccessResponseExplanation(responseClass = CategoryDocumentResponse.class, description = "조회 성공"))
        ResponseEntity<ApiResponse<CategoryDocumentResponse>> getDailyDocument(
                        @PathVariable Long categoryId,
                        @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user);

        @Operation(summary = "유저 맞춤 카테고리 자료(요약글) 생성", description = "관리자용(ADMIN), 유저의 Goal 프롬프트에 기반하여 자료를 생성합니다.")
        @ApiResponseExplanations(success = @ApiSuccessResponseExplanation(description = "생성 성공"))
        ResponseEntity<ApiResponse<Void>> createDocument(
                        @PathVariable Long categoryId,
                        @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user);

        @Operation(summary = "전체 카테고리 자료(요약글) 삭제", description = "관리자(ADMIN)만 삭제할 수 있습니다.")
        @ApiResponseExplanations(success = @ApiSuccessResponseExplanation(description = "삭제 성공"))
        ResponseEntity<ApiResponse<Void>> deleteDocument(
                        @PathVariable Long documentId,
                        @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user);
}
