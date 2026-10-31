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
    // 공지 목록 한 번에 조회할 최대 공지 수
    private static final int MAX_SLICE_SIZE = 50;

    private final NoticeRepository noticeRepository;

    // 커서 기반 최신순 조회, lastNoticeId가 없으면 첫 페이지이고 있으면 그보다 오래된 공지를 조회
    public Slice<Notice> getNoticeSlice(Long lastNoticeId, int size) {
        Pageable pageable = PageRequest.of(0, Math.min(Math.max(size, 1), MAX_SLICE_SIZE), LATEST_FIRST);
        if (lastNoticeId == null) {
            return noticeRepository.findSliceByIsDeletedFalse(pageable);
        }
        return noticeRepository.findSliceByIsDeletedFalseAndIdLessThan(lastNoticeId, pageable);
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
