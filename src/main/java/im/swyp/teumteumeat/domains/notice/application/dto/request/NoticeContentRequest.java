package im.swyp.teumteumeat.domains.notice.application.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 공지 등록/수정 요청의 공통 필드와 필드 간 검증 규칙
 */
public interface NoticeContentRequest {

    String title();

    String fullScreenImageKey();

    String modalImageKey();

    String externalLink();

    String deepLink();

    String actionButtonText();

    LocalDateTime popupStartAt();

    LocalDateTime popupEndAt();

    @JsonIgnore
    @AssertTrue(message = "외부 URL과 딥링크는 동시에 지정할 수 없습니다.")
    default boolean isSingleActionLink() {
        return !(StringUtils.hasText(externalLink()) && StringUtils.hasText(deepLink()));
    }

    @JsonIgnore
    default boolean hasActionLink() {
        return StringUtils.hasText(externalLink()) || StringUtils.hasText(deepLink());
    }

    @JsonIgnore
    @AssertTrue(message = "외부 URL이나 딥링크가 있으면 액션 버튼 문구가 필요합니다.")
    default boolean isActionButtonTextPresent() {
        return !hasActionLink() || StringUtils.hasText(actionButtonText());
    }

    @JsonIgnore
    @AssertTrue(message = "외부 URL은 http:// 또는 https://로 시작해야 합니다.")
    default boolean isExternalLinkValid() {
        if (!StringUtils.hasText(externalLink())) {
            return true;
        }
        return externalLink().startsWith("http://") || externalLink().startsWith("https://");
    }

    @JsonIgnore
    @AssertTrue(message = "팝업 시작 일시는 종료 일시보다 앞서야 합니다.")
    default boolean isPopupPeriodValid() {
        // 미입력은 @NotNull에서 검증
        if (popupStartAt() == null || popupEndAt() == null) {
            return true;
        }
        return popupStartAt().isBefore(popupEndAt());
    }
}
