package im.swyp.teumteumeat.domains.document.presentation.controller;

import im.swyp.teumteumeat.domains.document.application.dto.response.DocumentDetailResponse;
import im.swyp.teumteumeat.domains.document.application.usecase.DocumentSummaryUseCase;
import im.swyp.teumteumeat.domains.document.presentation.api.DocumentSummaryApi;
import im.swyp.teumteumeat.global.common.ApiResponse;
import im.swyp.teumteumeat.global.common.CommonResponseCode;
import im.swyp.teumteumeat.global.security.dto.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/goals")
public class DocumentSummaryController implements DocumentSummaryApi {

    private final DocumentSummaryUseCase documentSummaryUseCase;

    @Override
    @PostMapping("/{goalId}/documents/{documentId}/summary")
    public ResponseEntity<ApiResponse<Void>> createSummary(
            @PathVariable Long goalId,
            @PathVariable Long documentId,
            @AuthenticationPrincipal CustomUserDetails user) {
        documentSummaryUseCase.createSummaryAsync(user.getUserId(), goalId, documentId);
        return ResponseEntity.accepted().body(ApiResponse.ofSuccess(CommonResponseCode.OK));
    }

    @Override
    @GetMapping("/{goalId}/documents/{documentId}/summary")
    public ResponseEntity<ApiResponse<DocumentDetailResponse>> getSummary(
            @PathVariable Long goalId,
            @PathVariable Long documentId,
            @AuthenticationPrincipal CustomUserDetails user) {
        DocumentDetailResponse response = documentSummaryUseCase.getSummaryForView(user.getUserId(), goalId,
                documentId);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, response));
    }
}
