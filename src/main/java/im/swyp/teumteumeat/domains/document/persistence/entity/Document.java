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

import im.swyp.teumteumeat.domains.quiz.persistence.entity.Quiz;

import java.util.ArrayList;
import java.util.List;

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

    @Column(length = 255)
    private String title;

    @Lob
    private String rawContent;

    @Column(length = 500)
    private String summary;

    @Enumerated(EnumType.STRING)
    private FileStatus status;

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Quiz> quizzes = new ArrayList<>();

    @Builder
    private Document(
            UserEntity user,
            Goal goal,
            String fileName,
            String fileKey,
            long fileSize,
            String title,
            String rawContent,
            String summary,
            FileStatus status) {
        this.user = user;
        this.goal = goal;
        this.fileName = fileName;
        this.fileKey = fileKey;
        this.fileSize = fileSize;
        this.title = title;
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
    }

    public void updateSummary(String summary) {
        this.summary = summary;
    }

    public void updateTitle(String title) {
        this.title = title;
    }
}
