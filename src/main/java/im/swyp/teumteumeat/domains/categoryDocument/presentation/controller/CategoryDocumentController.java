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

//    @GetMapping(value = "/{categoryId}/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    public SseEmitter subscribe(@PathVariable Long categoryId, @AuthenticationPrincipal CustomUserDetails user,
//                                @RequestHeader(value = "Last-Event-ID", required = false) String lastEventId, HttpServletResponse response){
//        return categoryDocumentUseCase.subscribe(user.getUserId(), categoryId, lastEventId);
//    }

    @Override
    @PostMapping("/{categoryId}/documents/daily")
    public ResponseEntity<ApiResponse<CategoryDocumentResponse>> generateDocument(
            @PathVariable Long categoryId,
            @AuthenticationPrincipal CustomUserDetails user) {
        CategoryDocumentResponse response = categoryDocumentUseCase.generateDocument(categoryId, user.getUserId());
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, response));
    }

    @Override
    @PostMapping(value = "/{categoryId}/documents/daily/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> generateDocumentStream(
            @PathVariable Long categoryId,
            @AuthenticationPrincipal CustomUserDetails user) {
        SseEmitter sseEmitter = categoryDocumentUseCase.generateDocumentStream(categoryId, user.getUserId());

        // Nginx 등 프록시 서버가 스트리밍 데이터를 모아두지 않고 즉시 통과시키도록 헤더 추가
        return ResponseEntity.ok()
                .header("X-Accel-Buffering", "no")
                .body(sseEmitter);
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
