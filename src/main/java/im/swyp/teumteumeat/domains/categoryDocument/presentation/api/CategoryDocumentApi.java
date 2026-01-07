package im.swyp.teumteumeat.domains.categoryDocument.presentation.api;

import im.swyp.teumteumeat.domains.categoryDocument.application.dto.response.CategoryDocumentResponse;
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

@Tag(name = "CategoryDocument", description = "카테고리 자료(요약글) API")
public interface CategoryDocumentApi {

        @Operation(summary = "오늘의 카테고리 요약글 생성 및 조회", description = "오늘 학습할 카테고리 요약글을 생성하거나 조회합니다. (1일 1회 제한)")
        @ApiResponseExplanations(success = @ApiSuccessResponseExplanation(responseClass = CategoryDocumentResponse.class, description = "생성 및 조회 성공"))
        ResponseEntity<ApiResponse<CategoryDocumentResponse>> generateDocument(
                        @PathVariable Long categoryId,
                        @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user);

        @Operation(summary = "일일 카테고리 자료 상세 조회", description = "오늘 부여된 카테고리 자료를 조회합니다. (퀴즈 풀이 전용)")
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
