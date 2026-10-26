package im.swyp.teumteumeat.domains.notice.domain.service;

import im.swyp.teumteumeat.domains.notice.domain.constant.NoticeResponseCode;
import im.swyp.teumteumeat.domains.notice.persistence.entity.Notice;
import im.swyp.teumteumeat.domains.notice.persistence.repository.NoticeRepository;
import im.swyp.teumteumeat.global.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private static final Sort LATEST_FIRST = Sort.by(Sort.Direction.DESC, "id");
    // 메인 모달로 노출할 최대 공지 수
    private static final int MAX_POPUP_NOTICE_COUNT = 1;

    private final NoticeRepository noticeRepository;

    public Slice<Notice> getNoticeSlice(Pageable pageable) {
        return noticeRepository.findSliceByIsDeletedFalse(latestFirst(pageable));
    }

    public Page<Notice> getNoticePage(Pageable pageable) {
        return noticeRepository.findPageByIsDeletedFalse(latestFirst(pageable));
    }

    // 팝업 기간 내 공지 중 최신순 최대 MAX_POPUP_NOTICE_COUNT개
    public List<Notice> getPopupNotices(LocalDateTime now) {
        return noticeRepository.findPopupNotices(now, PageRequest.of(0, MAX_POPUP_NOTICE_COUNT));
    }

    public void createNotice(Notice notice) {
        noticeRepository.save(notice);
    }

    public void updateNotice(Long noticeId, Notice source) {
        Notice notice = getOrThrow(noticeId);
        notice.updateNotice(source);
    }

    public void deleteNotice(Long noticeId) {
        Notice notice = getOrThrow(noticeId);
        notice.delete();
    }

    /* HELPER METHOD */
    // 정렬은 최신순(id DESC) 고정, 요청의 sort는 무시
    private Pageable latestFirst(Pageable pageable) {
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), LATEST_FIRST);
    }

    private Notice getOrThrow(Long id) {
        return noticeRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new BaseException(NoticeResponseCode.NOT_FOUND_NOTICE));
    }
}
