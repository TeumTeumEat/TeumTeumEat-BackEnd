package im.swyp.teumteumeat.domains.document.presentation.api;

import im.swyp.teumteumeat.domains.document.application.dto.response.DocumentDetailResponse;
import im.swyp.teumteumeat.global.annotation.swagger.ApiResponseExplanations;
import im.swyp.teumteumeat.global.annotation.swagger.ApiSuccessResponseExplanation;
import im.swyp.teumteumeat.global.common.ApiResponse;
import im.swyp.teumteumeat.global.security.dto.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Document Summary(PDF)", description = "문서 요약 API")
@RequestMapping("/api/v1/goals/{goalId}/documents")
public interface DocumentSummaryApi {

    @Operation(summary = "문서 요약 생성 (학습 시작)", description = "문서의 요약본을 생성합니다. 이 시점부터 학습이 시작된 것으로 간주됩니다. (일일 제한 적용)")
    @ApiResponseExplanations(success = @ApiSuccessResponseExplanation(responseClass = DocumentDetailResponse.class, description = "생성 성공"))
    @PostMapping("/{documentId}/summary")
    ResponseEntity<ApiResponse<DocumentDetailResponse>> createSummary(
            @PathVariable Long goalId,
            @PathVariable Long documentId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user);

    @Operation(summary = "문서 요약 재조회 (단순 조회)", description = "학습 중인 문서의 요약본을 다시 조회합니다. (퀴즈 풀이 전용)")
    @ApiResponseExplanations(success = @ApiSuccessResponseExplanation(responseClass = DocumentDetailResponse.class, description = "조회 성공"))
    @GetMapping("/{documentId}/summary")
    ResponseEntity<ApiResponse<DocumentDetailResponse>> getSummary(
            @PathVariable Long goalId,
            @PathVariable Long documentId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user);
}
