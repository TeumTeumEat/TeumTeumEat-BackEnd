package im.swyp.teumteumeat.domains.notice.presentation.controller;

import im.swyp.teumteumeat.domains.notice.application.dto.request.NoticeCreateRequest;
import im.swyp.teumteumeat.domains.notice.application.dto.request.NoticeUpdateRequest;
import im.swyp.teumteumeat.domains.notice.application.dto.response.NoticeModalListResponse;
import im.swyp.teumteumeat.domains.notice.application.dto.response.NoticePageResponse;
import im.swyp.teumteumeat.domains.notice.application.dto.response.NoticeSliceResponse;
import im.swyp.teumteumeat.domains.notice.application.usecase.NoticeUseCase;
import im.swyp.teumteumeat.domains.notice.presentation.api.NoticeApi;
import im.swyp.teumteumeat.global.common.ApiResponse;
import im.swyp.teumteumeat.global.common.CommonResponseCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class NoticeController implements NoticeApi {

    private final NoticeUseCase noticeUseCase;

    @Override
    @GetMapping("/notices")
    public ResponseEntity<ApiResponse<NoticeSliceResponse>> getNotices(
            @RequestParam(required = false) Long lastNoticeId,
            @RequestParam(defaultValue = "20") int size
    ) {
        NoticeSliceResponse response = noticeUseCase.getNoticeSlice(lastNoticeId, size);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, response));
    }

    @Override
    @GetMapping("/notices/modal")
    public ResponseEntity<ApiResponse<NoticeModalListResponse>> getNoticeModals() {
        NoticeModalListResponse response = noticeUseCase.getNoticeModals();
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, response));
    }

    @Override
    @GetMapping("/admin/notices")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<NoticePageResponse>> getNoticesForAdmin(
            Pageable pageable
    ) {
        NoticePageResponse response = noticeUseCase.getNoticePage(pageable);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, response));
    }

    @Override
    @PostMapping("/admin/notices")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> createNotice(
            @RequestBody @Valid NoticeCreateRequest request
    ) {
        noticeUseCase.createNotice(request);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK));
    }

    @Override
    @PatchMapping("/admin/notices/{noticeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> updateNotice(
            @PathVariable Long noticeId,
            @RequestBody @Valid NoticeUpdateRequest request
    ) {
        noticeUseCase.updateNotice(noticeId, request);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK));
    }

    @Override
    @DeleteMapping("/admin/notices/{noticeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteNotice(
            @PathVariable Long noticeId
    ) {
        noticeUseCase.deleteNotice(noticeId);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK));
    }
}
