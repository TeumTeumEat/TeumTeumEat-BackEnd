package im.swyp.teumteumeat.domains.notice.domain.service;

import im.swyp.teumteumeat.domains.notice.persistence.repository.NoticeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class NoticeServiceTest {

    private static final PageRequest SORTED_REQUEST = PageRequest.of(1, 10, Sort.by("fullScreenImageKey"));
    private static final PageRequest LATEST_FIRST = PageRequest.of(1, 10, Sort.by(Sort.Direction.DESC, "id"));

    private final NoticeRepository noticeRepository = mock(NoticeRepository.class);
    private final NoticeService noticeService = new NoticeService(noticeRepository);

    @Test
    @DisplayName("공지 목록은 요청의 sort를 무시하고 최신순(id DESC)으로 조회한다")
    void sliceLatestFirst() {
        noticeService.getNoticeSlice(SORTED_REQUEST);

        verify(noticeRepository).findSliceByIsDeletedFalse(LATEST_FIRST);
    }

    @Test
    @DisplayName("관리자 공지 목록은 요청의 sort를 무시하고 최신순(id DESC)으로 조회한다")
    void pageLatestFirst() {
        noticeService.getNoticePage(SORTED_REQUEST);

        verify(noticeRepository).findPageByIsDeletedFalse(LATEST_FIRST);
    }
}
