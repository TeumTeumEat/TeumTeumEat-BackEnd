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

@Tag(name = "Document Summary(PDF)", description = "PDF 요약 API")
@RequestMapping("/api/v1/goals/{goalId}/documents")
public interface DocumentSummaryApi {

        @Operation(summary = "PDF 요약 생성 (학습 시작)", description = "PDF의 요약본을 생성합니다. (광고 시청에 따른 퀴즈 풀이 허용 횟수 적용)")
        @ApiResponseExplanations(success = @ApiSuccessResponseExplanation(responseClass = DocumentDetailResponse.class, description = "생성 성공"))
        @PostMapping("/{documentId}/summary")
        ResponseEntity<ApiResponse<DocumentDetailResponse>> createSummary(
                        @PathVariable Long goalId,
                        @PathVariable Long documentId,
                        @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user);

        @Operation(summary = "PDF 요약 조회", description = "PDF의 요약본을 조회합니다.")
        @ApiResponseExplanations(success = @ApiSuccessResponseExplanation(responseClass = DocumentDetailResponse.class, description = "조회 성공"))
        @GetMapping("/{documentId}/summary")
        ResponseEntity<ApiResponse<DocumentDetailResponse>> getSummary(
                        @PathVariable Long goalId,
                        @PathVariable Long documentId,
                        @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user);
}
