package im.swyp.teumteumeat.domains.goal.persistence.entity;

import im.swyp.teumteumeat.domains.category.persistence.entity.Category;
import im.swyp.teumteumeat.domains.categoryDocument.persistence.entity.CategoryDocument;
import im.swyp.teumteumeat.domains.document.persistence.entity.Document;
import im.swyp.teumteumeat.domains.goal.domain.constant.Difficulty;
import im.swyp.teumteumeat.domains.goal.domain.constant.GoalType;
import im.swyp.teumteumeat.domains.user.persistence.entity.UserEntity;
import im.swyp.teumteumeat.global.base.entity.BaseEntity;
import im.swyp.teumteumeat.global.exception.BaseException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static im.swyp.teumteumeat.global.common.CommonResponseCode.FORBIDDEN;

@Entity
@Getter
@Table(name = "goal")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Goal extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "goal_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    private GoalType type;

    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(length = 30)
    private String prompt;

    @OneToMany(mappedBy = "goal", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Document> documents = new ArrayList<>();

    @OneToMany(mappedBy = "goal", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CategoryDocument> categoryDocuments = new ArrayList<>();

    @Column(columnDefinition = "int default 0")
    private Integer targetQuizSetCount;

    @Column(columnDefinition = "int default 0")
    private Integer completedQuizSetCount;

    @Column(columnDefinition = "boolean default false")
    private boolean isCompleted;

    @Builder
    private Goal(
            UserEntity user,
            GoalType type,
            LocalDate endDate,
            Difficulty difficulty,
            String prompt,
            Category category,
            Integer targetQuizSetCount) {
        this.user = user;
        this.type = type;
        this.endDate = endDate;
        this.difficulty = difficulty;
        this.prompt = prompt;
        this.category = category;
        this.targetQuizSetCount = targetQuizSetCount != null ? targetQuizSetCount : 0;
        this.completedQuizSetCount = 0;
        this.isCompleted = false;
    }

    public void validateOwner(Long userId) {
        if (userId == null || this.user == null || !userId.equals(this.user.getId())) {
            throw new BaseException(FORBIDDEN);
        }
    }

    public void updateGoal(
            LocalDate endDate,
            Difficulty difficulty,
            String prompt) {
        this.endDate = (endDate != null) ? endDate : this.endDate;
        this.difficulty = (difficulty != null) ? difficulty : this.difficulty;
        this.prompt = (prompt != null) ? prompt : this.prompt;
    }

    public void incrementCompletedQuizSetCount() {
        if (!this.isCompleted) {
            this.completedQuizSetCount = (this.completedQuizSetCount != null ? this.completedQuizSetCount : 0) + 1;

            int target = this.targetQuizSetCount != null ? this.targetQuizSetCount : 0;
            if (this.completedQuizSetCount >= target && target > 0) {
                this.isCompleted = true;
            }
        }
    }
}
