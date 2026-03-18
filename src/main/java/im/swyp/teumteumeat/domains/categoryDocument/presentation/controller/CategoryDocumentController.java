package im.swyp.teumteumeat.domains.categoryDocument.presentation.controller;

import im.swyp.teumteumeat.domains.categoryDocument.application.dto.response.CategoryDocumentResponse;
import im.swyp.teumteumeat.domains.categoryDocument.application.usecase.CategoryDocumentUseCase;
import im.swyp.teumteumeat.domains.categoryDocument.presentation.api.CategoryDocumentApi;
import im.swyp.teumteumeat.global.common.ApiResponse;
import im.swyp.teumteumeat.global.common.CommonResponseCode;
import im.swyp.teumteumeat.global.security.dto.CustomUserDetails;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryDocumentController implements CategoryDocumentApi {

    private final CategoryDocumentUseCase categoryDocumentUseCase;

    @GetMapping(value = "/{categoryId}/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@PathVariable Long categoryId, @AuthenticationPrincipal CustomUserDetails user,
                                @RequestHeader(value = "Last-Event-ID", required = false) String lastEventId, HttpServletResponse response){
        return categoryDocumentUseCase.subscribe(user.getUserId(), categoryId, lastEventId);
    }

    @Override
    @PostMapping("/{categoryId}/documents/daily")
    public ResponseEntity<ApiResponse<Void>> generateDocument(
            @PathVariable Long categoryId,
            @AuthenticationPrincipal CustomUserDetails user) {
        categoryDocumentUseCase.generateDocumentAsync(categoryId, user.getUserId());
        return ResponseEntity.accepted().body(ApiResponse.ofSuccess(CommonResponseCode.OK));
    }

    @Override
    @GetMapping("/{categoryId}/documents/daily")
    public ResponseEntity<ApiResponse<CategoryDocumentResponse>> getDailyDocument(
            @PathVariable Long categoryId,
            @AuthenticationPrincipal CustomUserDetails user) {
        CategoryDocumentResponse response = categoryDocumentUseCase.getDailyDocument(categoryId, user.getUserId());
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, response));
    }

    @Override
    @PostMapping("/{categoryId}/documents")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> createDocument(
            @PathVariable Long categoryId,
            @AuthenticationPrincipal CustomUserDetails user) {
        categoryDocumentUseCase.createDocument(categoryId, user.getUserId());
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK));
    }

    @Override
    @DeleteMapping("/documents/{documentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteDocument(
            @PathVariable Long documentId,
            @AuthenticationPrincipal CustomUserDetails user) {
        categoryDocumentUseCase.deleteDocument(documentId);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK));
    }
}
