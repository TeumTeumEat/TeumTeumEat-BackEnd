package im.swyp.teumteumeat.domains.notice.application.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class NoticeContentRequestTest {

    private static final LocalDateTime START = LocalDateTime.of(2026, 10, 1, 0, 0);
    private static final LocalDateTime END = LocalDateTime.of(2026, 10, 31, 23, 59, 59);

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void 제목과_이미지_두개만_있으면_등록할_수_있다() {
        assertThat(violatedProperties(request("full.png", "modal.png", null, null))).isEmpty();
    }

    @Test
    void 전체화면_이미지와_모달_이미지는_필수다() {
        assertThat(violatedProperties(request(null, " ", null, null)))
                .containsExactlyInAnyOrder("fullScreenImageKey", "modalImageKey");
    }

    @Test
    void 외부_URL과_딥링크는_동시에_지정할_수_없다() {
        assertThat(violatedProperties(request("full.png", "modal.png", "https://example.com", "teumteumeat://quiz")))
                .containsExactly("singleActionLink");
    }

    @Test
    void 딥링크만_있으면_http로_시작하지_않아도_된다() {
        assertThat(violatedProperties(request("full.png", "modal.png", null, "teumteumeat://quiz"))).isEmpty();
    }

    @Test
    void 외부_URL은_http_또는_https로_시작해야_한다() {
        assertThat(violatedProperties(request("full.png", "modal.png", "https://example.com", null))).isEmpty();
        assertThat(violatedProperties(request("full.png", "modal.png", "example.com", null)))
                .containsExactly("externalLinkValid");
    }

    @Test
    void 링크가_있으면_액션_버튼_문구가_필요하다() {
        assertThat(violatedProperties(buttonRequest("teumteumeat://quiz", " ")))
                .containsExactly("actionButtonTextPresent");
    }

    @Test
    void 링크가_없으면_액션_버튼_문구가_없어도_된다() {
        assertThat(violatedProperties(buttonRequest(null, null))).isEmpty();
    }

    @Test
    void 팝업_시작_일시와_종료_일시는_필수다() {
        assertThat(violatedProperties(popupRequest(null, null)))
                .containsExactlyInAnyOrder("popupStartAt", "popupEndAt");
    }

    @Test
    void 팝업_시작_일시는_종료_일시보다_앞서야_한다() {
        assertThat(violatedProperties(popupRequest(START, END))).isEmpty();
        assertThat(violatedProperties(popupRequest(END, START))).containsExactly("popupPeriodValid");
        assertThat(violatedProperties(popupRequest(START, START))).containsExactly("popupPeriodValid");
    }

    private NoticeCreateRequest request(String fullScreenImageKey, String modalImageKey, String externalLink, String deepLink) {
        return new NoticeCreateRequest("제목", fullScreenImageKey, modalImageKey, externalLink, deepLink, "자세히 보기", START, END);
    }

    private NoticeCreateRequest popupRequest(LocalDateTime popupStartAt, LocalDateTime popupEndAt) {
        return new NoticeCreateRequest("제목", "full.png", "modal.png", null, null, null, popupStartAt, popupEndAt);
    }

    private NoticeCreateRequest buttonRequest(String deepLink, String actionButtonText) {
        return new NoticeCreateRequest("제목", "full.png", "modal.png", null, deepLink, actionButtonText, START, END);
    }

    private Set<String> violatedProperties(NoticeContentRequest request) {
        return validator.validate(request).stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .collect(Collectors.toSet());
    }
}
