package im.swyp.teumteumeat.domains.document.presentation.controller;

import im.swyp.teumteumeat.domains.document.application.dto.response.DocumentDetailResponse;
import im.swyp.teumteumeat.domains.document.application.usecase.DocumentSummaryUseCase;
import im.swyp.teumteumeat.domains.document.presentation.api.DocumentSummaryApi;
import im.swyp.teumteumeat.global.common.ApiResponse;
import im.swyp.teumteumeat.global.common.CommonResponseCode;
import im.swyp.teumteumeat.global.security.dto.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/goals/{goalId}/documents")
public class DocumentSummaryController implements DocumentSummaryApi {

    private final DocumentSummaryUseCase documentSummaryUseCase;

    @Override
    @PostMapping("/{documentId}/summary")
    public ResponseEntity<ApiResponse<DocumentDetailResponse>> createSummary(Long goalId, Long documentId,
            CustomUserDetails user) {
        DocumentDetailResponse response = documentSummaryUseCase.createSummary(user.getUserId(), goalId, documentId);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, response));
    }

    @Override
    @GetMapping("/{documentId}/summary")
    public ResponseEntity<ApiResponse<DocumentDetailResponse>> getSummary(Long goalId, Long documentId,
            CustomUserDetails user) {
        DocumentDetailResponse response = documentSummaryUseCase.getSummaryForView(user.getUserId(), goalId,
                documentId);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, response));
    }
}
