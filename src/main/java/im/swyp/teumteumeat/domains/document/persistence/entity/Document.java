package im.swyp.teumteumeat.domains.document.persistence.entity;

import im.swyp.teumteumeat.domains.document.domain.constant.FileStatus;
import im.swyp.teumteumeat.domains.goal.persistence.entity.Goal;
import im.swyp.teumteumeat.domains.user.persistence.entity.UserEntity;
import im.swyp.teumteumeat.global.base.entity.BaseEntity;
import im.swyp.teumteumeat.global.exception.BaseException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static im.swyp.teumteumeat.global.common.CommonResponseCode.FORBIDDEN;

@Entity
@Getter
@Table(name = "document")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Document extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "document_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id", nullable = false)
    private Goal goal;

    private String fileName;

    private String fileKey;

    private long fileSize;

    @Lob
    private String rawContent;

    @Lob
    private String summary;

    @Enumerated(EnumType.STRING)
    private FileStatus status;

    @Builder
    private Document(
            UserEntity user,
            Goal goal,
            String fileName,
            String fileKey,
            long fileSize,
            String rawContent,
            String summary,
            FileStatus status
    ) {
        this.user = user;
        this.goal = goal;
        this.fileName = fileName;
        this.fileKey = fileKey;
        this.fileSize = fileSize;
        this.rawContent = rawContent;
        this.summary = summary;
        this.status = status;
    }

    public void validateOwner(Long userId) {
        if (!this.user.getId().equals(userId)) {
            throw new BaseException(FORBIDDEN);
        }
    }

    public void updateStatus(FileStatus status) {
        this.status = status;
    }

    public void updateRawContent(String rawContent) {
        this.rawContent = rawContent;
        this.updateStatus(FileStatus.COMPLETED);
    }

    public void updateSummary(String summary) {
        this.summary = summary;
    }
}
