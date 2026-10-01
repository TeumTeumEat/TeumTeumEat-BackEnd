package im.swyp.teumteumeat.domains.notice.persistence.entity;

import im.swyp.teumteumeat.global.base.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @Column(length = 5000)
    private String content;

    @Column(columnDefinition = "boolean default false")
    private boolean isDeleted;

    @Builder
    private Notice(
            String title,
            String content
    ) {
        this.title = title;
        this.content = content;
        this.isDeleted = false;
    }

    public void updateTitle(String title) {
        this.title = title;
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void delete() {
        this.isDeleted = true;
    }
}
