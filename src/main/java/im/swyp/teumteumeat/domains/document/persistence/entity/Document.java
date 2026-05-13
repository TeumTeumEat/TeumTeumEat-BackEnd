package im.swyp.teumteumeat.domains.document.persistence.entity;

import im.swyp.teumteumeat.domains.category.domain.constant.DocumentErrorType;
import im.swyp.teumteumeat.domains.document.domain.constant.DocumentResponseCode;
import im.swyp.teumteumeat.domains.document.domain.constant.FileStatus;
import im.swyp.teumteumeat.domains.goal.persistence.entity.Goal;
import im.swyp.teumteumeat.domains.quiz.persistence.entity.Quiz;
import im.swyp.teumteumeat.domains.user.persistence.entity.UserEntity;
import im.swyp.teumteumeat.global.base.entity.BaseEntity;
import im.swyp.teumteumeat.global.exception.BaseException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    @JoinColumn(name = "user_id", nullable = true)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id", nullable = true)
    private Goal goal;

    private String fileName;

    private String fileKey;

    @Enumerated(EnumType.STRING)
    private FileStatus status;

    @Enumerated(EnumType.STRING)
    private DocumentErrorType errorReason;

    private Integer totalParts;

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DocumentPart> parts = new ArrayList<>();

    @Column(length = 255)
    private String title;

    @Lob
    private String rawContent;

    private LocalDateTime estimateTime;

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DocumentSummary> summaries = new ArrayList<>();

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Quiz> quizzes = new ArrayList<>();

    @Builder
    private Document(
            UserEntity user,
            Goal goal,
            String fileName,
            String fileKey,
            String title,
            String rawContent,
            FileStatus status) {
        this.user = user;
        this.goal = goal;
        this.fileName = fileName;
        this.fileKey = fileKey;
        this.title = title;
        this.rawContent = rawContent;
        this.status = status;
    }

    public void validateOwner(Long userId) {
        if (userId == null || this.user == null || !userId.equals(this.user.getId())) {
            throw new BaseException(FORBIDDEN);
        }
    }

    public void validateBelongTo(Long goalId) {
        if (this.goal == null || !this.goal.getId().equals(goalId)) {
            throw new BaseException(DocumentResponseCode.INVALID_DOCUMENT_GOAL_ASSOCIATION);
        }
    }

    public void updateUser(UserEntity user) {
        this.user = user;
    }

    public void updateGoal(Goal goal) {
        this.goal = goal;
    }

    public void updateStatus(FileStatus status) {
        this.status = status;
        if (status != FileStatus.FAILED) {
            this.errorReason = null;
        }
    }

    public void updateStatusToFailed(DocumentErrorType reason) {
        Objects.requireNonNull(reason);

        this.status = FileStatus.FAILED;
        this.errorReason = reason;
    }

    public void updateRawContent(String rawContent) {
        this.rawContent = rawContent;
    }

    public void updateTitle(String title) {
        this.title = title;
    }

    public void updateTotalParts(Integer totalParts) {
        this.totalParts = totalParts;
    }

    public void updateEstimateTime(Integer estimateTime) {
        this.estimateTime = LocalDateTime.now().plus(estimateTime, ChronoUnit.MILLIS);
    }

    public void deleteEstimateTime() {
        this.estimateTime = null;
    }

    public boolean isAllPartsCollected() {
        return parts.size() == totalParts;
    }
}
