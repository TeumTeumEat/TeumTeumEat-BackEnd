package im.swyp.teumteumeat.domains.notice.application.usecase;

import im.swyp.teumteumeat.domains.notice.application.dto.request.NoticeCreateRequest;
import im.swyp.teumteumeat.domains.notice.application.dto.request.NoticeUpdateRequest;
import im.swyp.teumteumeat.domains.notice.application.dto.response.NoticePageResponse;
import im.swyp.teumteumeat.domains.notice.application.dto.response.NoticeSliceResponse;
import im.swyp.teumteumeat.domains.notice.application.mapper.NoticeMapper;
import im.swyp.teumteumeat.domains.notice.domain.service.NoticeService;
import im.swyp.teumteumeat.global.annotation.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeUseCase {

    private final NoticeService noticeService;

    public NoticeSliceResponse getNoticeSlice(Pageable pageable) {
        return NoticeMapper.toNoticeSliceResponse(noticeService.getNoticeSlice(pageable));
    }

    public NoticePageResponse getNoticePage(Pageable pageable) {
        return NoticeMapper.toNoticePageResponse(noticeService.getNoticePage(pageable));
    }

    @Transactional
    public void createNotice(NoticeCreateRequest request) {
        noticeService.createNotice(NoticeMapper.toNotice(request));
    }

    @Transactional
    public void updateNotice(Long noticeId, NoticeUpdateRequest request) {
        noticeService.updateNotice(noticeId, request.title(), request.content());
    }

    @Transactional
    public void deleteNotice(Long noticeId) {
        noticeService.deleteNotice(noticeId);
    }
}
