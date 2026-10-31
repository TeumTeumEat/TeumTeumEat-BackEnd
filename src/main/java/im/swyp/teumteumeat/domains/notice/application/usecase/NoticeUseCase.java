package im.swyp.teumteumeat.domains.notice.application.usecase;

import im.swyp.teumteumeat.domains.notice.application.dto.request.NoticeCreateRequest;
import im.swyp.teumteumeat.domains.notice.application.dto.request.NoticeUpdateRequest;
import im.swyp.teumteumeat.domains.notice.application.dto.response.NoticeAdminResponse;
import im.swyp.teumteumeat.domains.notice.application.dto.response.NoticeDetailResponse;
import im.swyp.teumteumeat.domains.notice.application.dto.response.NoticeModalListResponse;
import im.swyp.teumteumeat.domains.notice.application.dto.response.NoticeModalResponse;
import im.swyp.teumteumeat.domains.notice.application.dto.response.NoticePageResponse;
import im.swyp.teumteumeat.domains.notice.application.dto.response.NoticeResponse;
import im.swyp.teumteumeat.domains.notice.application.dto.response.NoticeSliceResponse;
import im.swyp.teumteumeat.domains.notice.application.mapper.NoticeMapper;
import im.swyp.teumteumeat.domains.notice.domain.service.NoticeService;
import im.swyp.teumteumeat.domains.notice.persistence.entity.Notice;
import im.swyp.teumteumeat.global.annotation.UseCase;
import im.swyp.teumteumeat.infra.s3.domain.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@UseCase
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeUseCase {

    private final NoticeService noticeService;
    private final S3Service s3Service;

    public NoticeSliceResponse getNoticeSlice(Long lastNoticeId, int size) {
        return NoticeMapper.toNoticeSliceResponse(noticeService.getNoticeSlice(lastNoticeId, size).map(this::toNoticeResponse));
    }

    // 팝업 기간 내 공지를 최신순으로 모달 노출, 공지가 없으면 빈 리스트
    public NoticeModalListResponse getNoticeModals() {
        return NoticeMapper.toNoticeModalListResponse(noticeService.getPopupNotices(LocalDateTime.now()).stream()
                .map(this::toNoticeModalResponse)
                .toList());
    }

    public NoticePageResponse getNoticePage(Pageable pageable) {
        return NoticeMapper.toNoticePageResponse(noticeService.getNoticePage(pageable).map(this::toNoticeAdminResponse));
    }

    @Transactional
    public void createNotice(NoticeCreateRequest request) {
        noticeService.createNotice(NoticeMapper.toNotice(request));
    }

    @Transactional
    public void updateNotice(Long noticeId, NoticeUpdateRequest request) {
        noticeService.updateNotice(noticeId, NoticeMapper.toNotice(request));
    }

    @Transactional
    public void deleteNotice(Long noticeId) {
        noticeService.deleteNotice(noticeId);
    }

    /* HELPER METHOD */
    private NoticeResponse toNoticeResponse(Notice notice) {
        return NoticeMapper.toNoticeResponse(notice, toNoticeDetailResponse(notice));
    }

    private NoticeModalResponse toNoticeModalResponse(Notice notice) {
        return NoticeMapper.toNoticeModalResponse(
                notice,
                s3Service.generateFileUrl(notice.getModalImageKey()),
                toNoticeDetailResponse(notice)
        );
    }

    private NoticeDetailResponse toNoticeDetailResponse(Notice notice) {
        return NoticeMapper.toNoticeDetailResponse(notice, s3Service.generateFileUrl(notice.getFullScreenImageKey()));
    }

    private NoticeAdminResponse toNoticeAdminResponse(Notice notice) {
        return NoticeMapper.toNoticeAdminResponse(
                notice,
                s3Service.generateFileUrl(notice.getFullScreenImageKey()),
                s3Service.generateFileUrl(notice.getModalImageKey())
        );
    }
}
