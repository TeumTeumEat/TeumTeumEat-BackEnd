package im.swyp.teumteumeat.domains.notice.application.mapper;

import im.swyp.teumteumeat.domains.notice.application.dto.request.NoticeCreateRequest;
import im.swyp.teumteumeat.domains.notice.persistence.entity.Notice;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class NoticeMapperTest {

    private static final LocalDateTime START = LocalDateTime.of(2026, 10, 1, 0, 0);
    private static final LocalDateTime END = LocalDateTime.of(2026, 10, 31, 23, 59, 59);

    @Test
    void 빈_문자열_링크는_null로_저장한다() {
        Notice notice = NoticeMapper.toNotice(new NoticeCreateRequest("제목", "full.png", "modal.png", " ", "", null, START, END));

        assertThat(notice.getExternalLink()).isNull();
        assertThat(notice.getDeepLink()).isNull();
    }

    @Test
    void 링크가_있으면_그대로_저장한다() {
        Notice notice = NoticeMapper.toNotice(
                new NoticeCreateRequest("제목", "full.png", "modal.png", null, "teumteumeat://quiz", "퀴즈 풀기", START, END));

        assertThat(notice.getExternalLink()).isNull();
        assertThat(notice.getDeepLink()).isEqualTo("teumteumeat://quiz");
        assertThat(notice.getActionButtonText()).isEqualTo("퀴즈 풀기");
    }

    @Test
    void 링크가_없으면_액션_버튼_문구를_저장하지_않는다() {
        Notice notice = NoticeMapper.toNotice(
                new NoticeCreateRequest("제목", "full.png", "modal.png", null, null, "자세히 보기", START, END));

        assertThat(notice.getActionButtonText()).isNull();
    }
}
