package im.swyp.teumteumeat.domains.document.presentation.controller;

import im.swyp.teumteumeat.domains.document.application.dto.request.DocumentCreateRequest;
import im.swyp.teumteumeat.domains.document.application.dto.response.DocumentDetailResponse;
import im.swyp.teumteumeat.domains.document.application.dto.response.DocumentIdResponse;
import im.swyp.teumteumeat.domains.document.application.dto.response.DocumentListResponse;
import im.swyp.teumteumeat.domains.document.application.dto.response.DocumentResponse;
import im.swyp.teumteumeat.domains.document.application.usecase.DocumentUseCase;
import im.swyp.teumteumeat.domains.document.presentation.api.DocumentApi;
import im.swyp.teumteumeat.global.common.ApiResponse;
import im.swyp.teumteumeat.global.common.CommonResponseCode;
import im.swyp.teumteumeat.global.security.dto.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/goals/{goalId}/documents")
@RequiredArgsConstructor
public class DocumentController implements DocumentApi {

    private final DocumentUseCase documentUseCase;

    @Override
    @PostMapping
    public ResponseEntity<ApiResponse<DocumentIdResponse>> uploadDocument(
            @PathVariable Long goalId,
            @RequestBody @Valid DocumentCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails user) {
        DocumentIdResponse response = documentUseCase.uploadDocument(user.getUserId(), goalId, request);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, response));
    }

    @Override
    @GetMapping
    public ResponseEntity<ApiResponse<DocumentListResponse>> getDocuments(
            @PathVariable Long goalId,
            @AuthenticationPrincipal CustomUserDetails user) {
        DocumentListResponse response = documentUseCase.getDocuments(user.getUserId(), goalId);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, response));
    }

    @Override
    @GetMapping("/{documentId}")
    public ResponseEntity<ApiResponse<DocumentResponse>> getDocument(
            @PathVariable Long goalId,
            @PathVariable Long documentId,
            @AuthenticationPrincipal CustomUserDetails user) {
        DocumentResponse response = documentUseCase.getDocument(user.getUserId(), goalId, documentId);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, response));
    }

    @Override
    @PostMapping("/{documentId}/summary")
    public ResponseEntity<ApiResponse<DocumentDetailResponse>> createSummary(
            @PathVariable Long goalId,
            @PathVariable Long documentId,
            @AuthenticationPrincipal CustomUserDetails user) {
        DocumentDetailResponse response = documentUseCase.createSummary(user.getUserId(), goalId, documentId);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, response));
    }

    @Override
    @GetMapping("/{documentId}/summary")
    public ResponseEntity<ApiResponse<DocumentDetailResponse>> getSummary(
            @PathVariable Long goalId,
            @PathVariable Long documentId,
            @AuthenticationPrincipal CustomUserDetails user) {
        DocumentDetailResponse response = documentUseCase.getSummaryForView(user.getUserId(), goalId, documentId);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, response));
    }

    @Override
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteDocuments(
            @PathVariable Long goalId,
            @AuthenticationPrincipal CustomUserDetails user) {
        documentUseCase.deleteDocuments(user.getUserId(), goalId);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK));
    }

    @Override
    @DeleteMapping("/{documentId}")
    public ResponseEntity<ApiResponse<Void>> deleteDocument(
            @PathVariable Long goalId,
            @PathVariable Long documentId,
            @AuthenticationPrincipal CustomUserDetails user) {
        documentUseCase.deleteDocument(user.getUserId(), goalId, documentId);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK));
    }
}
