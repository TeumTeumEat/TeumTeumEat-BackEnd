package im.swyp.teumteumeat.domains.document.presentation;

import im.swyp.teumteumeat.domains.document.application.dto.request.DocumentCreateRequest;
import im.swyp.teumteumeat.domains.document.application.dto.response.DocumentListResponse;
import im.swyp.teumteumeat.domains.document.application.dto.response.DocumentResponse;
import im.swyp.teumteumeat.domains.document.application.usecase.DocumentUseCase;
import im.swyp.teumteumeat.global.common.ApiResponse;
import im.swyp.teumteumeat.global.common.CommonResponseCode;
import im.swyp.teumteumeat.global.security.dto.CustomUserDetails;
import im.swyp.teumteumeat.domains.quiz.application.dto.response.QuizListResponse;
import im.swyp.teumteumeat.domains.quiz.application.usecase.QuizUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/goals/{goalId}/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentUseCase documentUseCase;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> uploadDocument(
            @PathVariable Long goalId,
            @RequestBody @Valid DocumentCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails user) {
        documentUseCase.uploadDocument(user.getUserId(), goalId, request);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<DocumentListResponse>> getDocuments(
            @PathVariable Long goalId,
            @AuthenticationPrincipal CustomUserDetails user) {
        DocumentListResponse response = documentUseCase.getDocuments(user.getUserId(), goalId);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, response));
    }

    @GetMapping("/{documentId}")
    public ResponseEntity<ApiResponse<DocumentResponse>> getDocument(
            @PathVariable Long goalId,
            @PathVariable Long documentId,
            @AuthenticationPrincipal CustomUserDetails user) {
        DocumentResponse response = documentUseCase.getDocument(user.getUserId(), goalId, documentId);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, response));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<DocumentListResponse>> deleteDocuments(
            @PathVariable Long goalId,
            @AuthenticationPrincipal CustomUserDetails user) {
        documentUseCase.deleteDocuments(user.getUserId(), goalId);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK));
    }

    @DeleteMapping("/{documentId}")
    public ResponseEntity<ApiResponse<DocumentListResponse>> deleteDocuments(
            @PathVariable Long goalId,
            @PathVariable Long documentId,
            @AuthenticationPrincipal CustomUserDetails user) {
        documentUseCase.deleteDocument(user.getUserId(), goalId, documentId);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK));
    }
}
