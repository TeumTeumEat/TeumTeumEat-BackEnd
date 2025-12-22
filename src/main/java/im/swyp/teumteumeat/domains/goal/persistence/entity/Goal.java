package im.swyp.teumteumeat.domains.goal.persistence.entity;

import im.swyp.teumteumeat.domains.category.persistence.entity.Category;
import im.swyp.teumteumeat.domains.document.persistence.entity.Document;
import im.swyp.teumteumeat.domains.goal.domain.constant.Difficulty;
import im.swyp.teumteumeat.domains.goal.domain.constant.GoalType;
import im.swyp.teumteumeat.domains.user.persistence.entity.UserEntity;
import im.swyp.teumteumeat.global.base.entity.BaseEntity;
import im.swyp.teumteumeat.global.exception.BaseException;
import jakarta.persistence.*;
import lombok.*;

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

    private String prompt;

    @OneToMany(mappedBy = "goal", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Document> documents = new ArrayList<>();

    @Builder
    private Goal(
            UserEntity user,
            GoalType type,
            LocalDate endDate,
            Difficulty difficulty,
            String prompt,
            Category category
    ) {
        this.user = user;
        this.type = type;
        this.endDate = endDate;
        this.difficulty = difficulty;
        this.prompt = prompt;
        this.category = category;
    }

    public void validateOwner(Long userId) {
        if (!this.user.getId().equals(userId)) {
            throw new BaseException(FORBIDDEN);
        }
    }

    public void updateGoal(
            LocalDate endDate,
            Difficulty difficulty,
            String prompt
    ) {
        this.endDate = (endDate != null) ? endDate : this.endDate;
        this.difficulty = (difficulty != null) ? difficulty : this.difficulty;
        this.prompt = (prompt != null) ? prompt : this.prompt;
    }
}
