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

        @Operation(summary = "일일 카테고리 요약글 생성 (학습 시작)", description = "오늘 학습할 새로운 카테고리 요약글과 퀴즈를 생성합니다. (광고 시청 등을 통해 얻은 퀴즈 풀이 가능 횟수 1회 차감)")
        @ApiResponseExplanations(success = @ApiSuccessResponseExplanation(responseClass = CategoryDocumentResponse.class, description = "생성 및 조회 성공"))
        ResponseEntity<ApiResponse<CategoryDocumentResponse>> generateDocument(
                        @PathVariable Long categoryId,
                        @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user);

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
