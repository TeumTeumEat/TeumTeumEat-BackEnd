package im.swyp.teumteumeat.domains.document.presentation.controller;

import im.swyp.teumteumeat.domains.document.application.dto.response.DocumentDetailResponse;
import im.swyp.teumteumeat.domains.document.application.usecase.DocumentSummaryUseCase;
import im.swyp.teumteumeat.domains.document.presentation.api.DocumentSummaryApi;
import im.swyp.teumteumeat.global.common.ApiResponse;
import im.swyp.teumteumeat.global.common.CommonResponseCode;
import im.swyp.teumteumeat.global.security.dto.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/goals")
public class DocumentSummaryController implements DocumentSummaryApi {

    private final DocumentSummaryUseCase documentSummaryUseCase;

    @Override
    @GetMapping(value = "/{goalId}/documents/{documentId}/summary/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(
            @PathVariable Long goalId,
            @PathVariable Long documentId,
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestHeader(value = "Last-Event-ID", required = false) String lastEventId,
            jakarta.servlet.http.HttpServletResponse response) {
        return documentSummaryUseCase.subscribe(user.getUserId(), goalId, documentId, lastEventId);
    }

    @Override
    @PostMapping("/{goalId}/documents/{documentId}/summary")
    public ResponseEntity<ApiResponse<DocumentDetailResponse>> createSummary(
            @PathVariable Long goalId,
            @PathVariable Long documentId,
            @AuthenticationPrincipal CustomUserDetails user) {
        DocumentDetailResponse response = documentSummaryUseCase.createSummary(user.getUserId(), goalId, documentId);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, response));
    }

    @Override
    @PostMapping("/{goalId}/documents/{documentId}/summary/stream")
    public ResponseEntity<ApiResponse<Void>> createSummaryStream(
            @PathVariable Long goalId,
            @PathVariable Long documentId,
            @AuthenticationPrincipal CustomUserDetails user) {
        DocumentDetailResponse response = documentSummaryUseCase.createSummary(user.getUserId(), goalId, documentId);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, response));
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
