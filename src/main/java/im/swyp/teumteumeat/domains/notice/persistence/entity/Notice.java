package im.swyp.teumteumeat.domains.notice.persistence.entity;

import im.swyp.teumteumeat.global.base.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "notice")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_id")
    private Long id;

    @Column(length = 100)
    private String title;

    /** 전체화면 이미지 S3 key */
    @Column(nullable = false)
    private String fullScreenImageKey;

    /** 모달 이미지 S3 key */
    @Column(nullable = false)
    private String modalImageKey;

    /** 전체화면 액션 버튼의 외부 URL, deepLink와 동시에 존재하지 않음 */
    @Column(length = 500)
    private String externalLink;

    /** 전체화면 액션 버튼의 앱 내부 딥링크, externalLink와 동시에 존재하지 않음 */
    @Column(length = 500)
    private String deepLink;

    /** 전체화면 액션 버튼 문구, 링크가 있을 때만 존재 */
    @Column(length = 20)
    private String actionButtonText;

    /** 메인 모달 노출 시작 일시 */
    @Column(nullable = false)
    private LocalDateTime popupStartAt;

    /** 메인 모달 노출 종료 일시, popupStartAt보다 뒤 */
    @Column(nullable = false)
    private LocalDateTime popupEndAt;

    @Column(columnDefinition = "boolean default false")
    private boolean isDeleted;

    @Builder
    private Notice(
            String title,
            String fullScreenImageKey,
            String modalImageKey,
            String externalLink,
            String deepLink,
            String actionButtonText,
            LocalDateTime popupStartAt,
            LocalDateTime popupEndAt
    ) {
        this.title = title;
        this.fullScreenImageKey = fullScreenImageKey;
        this.modalImageKey = modalImageKey;
        this.externalLink = externalLink;
        this.deepLink = deepLink;
        this.actionButtonText = actionButtonText;
        this.popupStartAt = popupStartAt;
        this.popupEndAt = popupEndAt;
        this.isDeleted = false;
    }

    public void updateNotice(Notice notice) {
        this.title = notice.getTitle();
        this.fullScreenImageKey = notice.getFullScreenImageKey();
        this.modalImageKey = notice.getModalImageKey();
        this.externalLink = notice.getExternalLink();
        this.deepLink = notice.getDeepLink();
        this.actionButtonText = notice.getActionButtonText();
        this.popupStartAt = notice.getPopupStartAt();
        this.popupEndAt = notice.getPopupEndAt();
    }

    public void delete() {
        this.isDeleted = true;
    }
}
