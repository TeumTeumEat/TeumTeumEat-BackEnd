package im.swyp.teumteumeat.domains.categoryDocument.presentation.controller;

import im.swyp.teumteumeat.domains.categoryDocument.application.dto.response.CategoryDocumentResponse;
import im.swyp.teumteumeat.domains.categoryDocument.application.usecase.CategoryDocumentUseCase;
import im.swyp.teumteumeat.domains.categoryDocument.presentation.api.CategoryDocumentApi;
import im.swyp.teumteumeat.global.common.ApiResponse;
import im.swyp.teumteumeat.global.common.CommonResponseCode;
import im.swyp.teumteumeat.global.security.dto.CustomUserDetails;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryDocumentController implements CategoryDocumentApi {

    private final CategoryDocumentUseCase categoryDocumentUseCase;

    @Override
    @GetMapping("/{categoryId}/documents")
    public ResponseEntity<ApiResponse<List<CategoryDocumentResponse>>> getDocuments(
            @PathVariable Long categoryId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user) {
        List<CategoryDocumentResponse> responses = categoryDocumentUseCase.getDocuments(categoryId, user.getUserId());
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, responses));
    }

    @Override
    @PostMapping("/{categoryId}/documents")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> createDocument(
            @PathVariable Long categoryId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user) {
        categoryDocumentUseCase.createDocument(categoryId);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK));
    }

    @Override
    @DeleteMapping("/documents/{documentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteDocument(
            @PathVariable Long documentId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user) {
        categoryDocumentUseCase.deleteDocument(documentId);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK));
    }
}
