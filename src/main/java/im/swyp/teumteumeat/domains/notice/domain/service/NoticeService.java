package im.swyp.teumteumeat.domains.notice.domain.service;

import im.swyp.teumteumeat.domains.notice.domain.constant.NoticeResponseCode;
import im.swyp.teumteumeat.domains.notice.persistence.entity.Notice;
import im.swyp.teumteumeat.domains.notice.persistence.repository.NoticeRepository;
import im.swyp.teumteumeat.global.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;

    public Slice<Notice> getNoticeSlice(Pageable pageable) {
        return noticeRepository.findSliceByIsDeletedFalse(pageable);
    }

    public Page<Notice> getNoticePage(Pageable pageable) {
        return noticeRepository.findPageByIsDeletedFalse(pageable);
    }

    public void createNotice(Notice notice) {
        noticeRepository.save(notice);
    }

    public void updateNotice(Long noticeId, String title, String content) {
        Notice notice = getOrThrow(noticeId);
        notice.updateTitle(title);
        notice.updateContent(content);
    }

    public void deleteNotice(Long noticeId) {
        Notice notice = getOrThrow(noticeId);
        notice.delete();
    }

    /* HELPER METHOD */
    private Notice getOrThrow(Long id) {
        return noticeRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new BaseException(NoticeResponseCode.NOT_FOUND_NOTICE));
    }
}
