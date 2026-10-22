package im.swyp.teumteumeat.domains.notice.application.usecase;

import im.swyp.teumteumeat.domains.notice.application.dto.response.NoticeAdminResponse;
import im.swyp.teumteumeat.domains.notice.application.dto.response.NoticeDetailResponse;
import im.swyp.teumteumeat.domains.notice.application.dto.response.NoticeModalResponse;
import im.swyp.teumteumeat.domains.notice.application.dto.response.NoticeResponse;
import im.swyp.teumteumeat.domains.notice.domain.service.NoticeService;
import im.swyp.teumteumeat.domains.notice.persistence.entity.Notice;
import im.swyp.teumteumeat.infra.s3.domain.service.S3Service;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.SliceImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NoticeUseCaseTest {

    private static final String FULL_SCREEN_IMAGE_KEY = "prod/notice/full.png";
    private static final String FULL_SCREEN_IMAGE_URL = "https://cdn.example.com/prod/notice/full.png";
    private static final String MODAL_IMAGE_KEY = "prod/notice/modal.png";
    private static final String MODAL_IMAGE_URL = "https://cdn.example.com/prod/notice/modal.png";

    private final NoticeService noticeService = mock(NoticeService.class);
    private final S3Service s3Service = mock(S3Service.class);
    private final NoticeUseCase noticeUseCase = new NoticeUseCase(noticeService, s3Service);

    {
        when(s3Service.generateFileUrl(FULL_SCREEN_IMAGE_KEY)).thenReturn(FULL_SCREEN_IMAGE_URL);
        when(s3Service.generateFileUrl(MODAL_IMAGE_KEY)).thenReturn(MODAL_IMAGE_URL);
    }

    @Test
    @DisplayName("모달 공지는 모달 이미지 URL과 함께 전체화면 공지를 detail로 반환한다")
    void modalWithDetail() {
        when(noticeService.getPopupNotice(any())).thenReturn(Optional.of(notice()));

        NoticeModalResponse response = noticeUseCase.getNoticeModal();

        assertThat(response)
                .extracting(NoticeModalResponse::title, NoticeModalResponse::modalImageUrl)
                .containsExactly("제목", MODAL_IMAGE_URL);
        assertThat(response.detail())
                .extracting(NoticeDetailResponse::fullScreenImageUrl, NoticeDetailResponse::externalLink,
                        NoticeDetailResponse::deepLink, NoticeDetailResponse::actionButtonText)
                .containsExactly(FULL_SCREEN_IMAGE_URL, null, "teumteumeat://quiz", "퀴즈 풀기");
    }

    @Test
    @DisplayName("팝업 기간 내 공지가 없으면 모달 공지는 null이다")
    void noModal() {
        when(noticeService.getPopupNotice(any())).thenReturn(Optional.empty());

        assertThat(noticeUseCase.getNoticeModal()).isNull();
    }

    @Test
    @DisplayName("공지 목록은 각 공지의 전체화면 공지를 detail로 함께 반환한다")
    void sliceWithDetail() {
        when(noticeService.getNoticeSlice(any()))
                .thenReturn(new SliceImpl<>(List.of(notice()), PageRequest.of(0, 20), false));

        List<NoticeResponse> notices = noticeUseCase.getNoticeSlice(PageRequest.of(0, 20)).notices();

        assertThat(notices)
                .extracting(NoticeResponse::detail)
                .extracting(NoticeDetailResponse::fullScreenImageUrl, NoticeDetailResponse::externalLink,
                        NoticeDetailResponse::deepLink, NoticeDetailResponse::actionButtonText)
                .containsExactly(tuple(FULL_SCREEN_IMAGE_URL, null, "teumteumeat://quiz", "퀴즈 풀기"));
    }

    @Test
    @DisplayName("관리자 공지 목록은 이미지 key와 미리보기용 URL을 함께 반환한다")
    void adminImageKeyAndUrl() {
        when(noticeService.getNoticePage(any()))
                .thenReturn(new PageImpl<>(List.of(notice()), PageRequest.of(0, 20), 1));

        List<NoticeAdminResponse> notices = noticeUseCase.getNoticePage(PageRequest.of(0, 20)).notices();

        assertThat(notices)
                .extracting(NoticeAdminResponse::fullScreenImageKey, NoticeAdminResponse::fullScreenImageUrl,
                        NoticeAdminResponse::modalImageKey, NoticeAdminResponse::modalImageUrl)
                .containsExactly(tuple(FULL_SCREEN_IMAGE_KEY, FULL_SCREEN_IMAGE_URL, MODAL_IMAGE_KEY, MODAL_IMAGE_URL));
    }

    private Notice notice() {
        return Notice.builder()
                .title("제목")
                .fullScreenImageKey(FULL_SCREEN_IMAGE_KEY)
                .modalImageKey(MODAL_IMAGE_KEY)
                .deepLink("teumteumeat://quiz")
                .actionButtonText("퀴즈 풀기")
                .popupStartAt(LocalDateTime.of(2026, 10, 1, 0, 0))
                .popupEndAt(LocalDateTime.of(2026, 10, 31, 23, 59, 59))
                .build();
    }
}
