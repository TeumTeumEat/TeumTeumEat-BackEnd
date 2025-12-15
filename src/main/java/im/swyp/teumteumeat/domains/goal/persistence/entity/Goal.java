package im.swyp.teumteumeat.domains.goal.persistence.entity;

import im.swyp.teumteumeat.domains.goal.domain.constant.GoalType;
import im.swyp.teumteumeat.domains.user.persistence.entity.UserEntity;
import im.swyp.teumteumeat.global.base.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

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

//    private Category category;

//    private List<Document> documents = new ArrayList<>();

    @Builder
    private Goal(
            UserEntity user,
            GoalType type,
            LocalDate endDate
    ) {
        this.user = user;
        this.type = type;
        this.endDate = endDate;
    }
}
