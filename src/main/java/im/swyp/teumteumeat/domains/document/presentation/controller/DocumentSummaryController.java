package im.swyp.teumteumeat.domains.document.presentation.controller;

import im.swyp.teumteumeat.domains.document.application.dto.response.DocumentDetailResponse;
import im.swyp.teumteumeat.domains.document.application.usecase.DocumentSummaryUseCase;
import im.swyp.teumteumeat.domains.document.presentation.api.DocumentSummaryApi;
import im.swyp.teumteumeat.global.common.ApiResponse;
import im.swyp.teumteumeat.global.common.CommonResponseCode;
import im.swyp.teumteumeat.global.security.annotation.LoginUser;
import im.swyp.teumteumeat.global.security.dto.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/goals")
public class DocumentSummaryController implements DocumentSummaryApi {

    private final DocumentSummaryUseCase documentSummaryUseCase;

    @Override
    @PostMapping("/{goalId}/documents/{documentId}/summary")
    public ResponseEntity<ApiResponse<DocumentDetailResponse>> createSummary(
            @PathVariable Long goalId,
            @PathVariable Long documentId,
            @LoginUser CustomUserDetails user) {
        DocumentDetailResponse response = documentSummaryUseCase.createSummary(user.getUserId(), goalId, documentId);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, response));
    }

    @Override
    @PostMapping(value = "/{goalId}/documents/{documentId}/summary/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> createSummaryStream(
            @PathVariable Long goalId,
            @PathVariable Long documentId,
            @LoginUser CustomUserDetails user) {
        SseEmitter sseEmitter = documentSummaryUseCase.createSummaryStream(user.getUserId(), goalId, documentId);

        // Nginx 등 프록시 서버가 스트리밍 데이터를 모아두지 않고 즉시 통과시키도록 헤더 추가
        return ResponseEntity.ok()
                .header("X-Accel-Buffering", "no")
                .body(sseEmitter);
    }

    @Override
    @GetMapping("/{goalId}/documents/{documentId}/summary")
    public ResponseEntity<ApiResponse<DocumentDetailResponse>> getSummary(
            @PathVariable Long goalId,
            @PathVariable Long documentId,
            @LoginUser CustomUserDetails user) {
        DocumentDetailResponse response = documentSummaryUseCase.getSummaryForView(user.getUserId(), goalId,
                documentId);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, response));
    }
}
