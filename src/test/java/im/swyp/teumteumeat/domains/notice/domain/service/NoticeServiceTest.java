package im.swyp.teumteumeat.domains.notice.domain.service;

import im.swyp.teumteumeat.domains.notice.persistence.repository.NoticeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class NoticeServiceTest {

    private static final PageRequest SORTED_REQUEST = PageRequest.of(1, 10, Sort.by("fullScreenImageKey"));
    private static final PageRequest LATEST_FIRST = PageRequest.of(1, 10, Sort.by(Sort.Direction.DESC, "id"));

    private final NoticeRepository noticeRepository = mock(NoticeRepository.class);
    private final NoticeService noticeService = new NoticeService(noticeRepository);

    @Test
    @DisplayName("lastNoticeId가 없으면 첫 페이지를 최신순(id DESC)으로 조회한다")
    void sliceFirstPage() {
        noticeService.getNoticeSlice(null, 20);

        verify(noticeRepository).findSliceByIsDeletedFalse(cursorRequest(20));
    }

    @Test
    @DisplayName("lastNoticeId가 있으면 그보다 작은 id의 공지를 최신순(id DESC)으로 조회한다")
    void sliceAfterCursor() {
        noticeService.getNoticeSlice(42L, 20);

        verify(noticeRepository).findSliceByIsDeletedFalseAndIdLessThan(42L, cursorRequest(20));
    }

    @ParameterizedTest(name = "size {0} -> {1}")
    @CsvSource({"0, 1", "-5, 1", "51, 50", "1000, 50"})
    @DisplayName("공지 목록 size는 1 ~ 50 범위로 보정한다")
    void sliceSizeClamped(int size, int expected) {
        noticeService.getNoticeSlice(null, size);

        verify(noticeRepository).findSliceByIsDeletedFalse(cursorRequest(expected));
    }

    @Test
    @DisplayName("관리자 공지 목록은 요청의 sort를 무시하고 최신순(id DESC)으로 조회한다")
    void pageLatestFirst() {
        noticeService.getNoticePage(SORTED_REQUEST);

        verify(noticeRepository).findPageByIsDeletedFalse(LATEST_FIRST);
    }

    @Test
    @DisplayName("모달 공지는 팝업 기간 내 공지를 최대 1개까지 조회한다")
    void popupNoticesLimit() {
        LocalDateTime now = LocalDateTime.of(2026, 10, 26, 9, 0);

        noticeService.getPopupNotices(now);

        verify(noticeRepository).findPopupNotices(now, PageRequest.of(0, 1));
    }

    private static PageRequest cursorRequest(int size) {
        return PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "id"));
    }
}
