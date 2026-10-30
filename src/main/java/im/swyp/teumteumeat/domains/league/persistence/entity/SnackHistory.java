package im.swyp.teumteumeat.domains.league.persistence.entity;

import im.swyp.teumteumeat.domains.user.persistence.entity.UserEntity;
import im.swyp.teumteumeat.global.base.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 퀴즈 세트 1회 완료 = 스낵 1개 적립 기록
 * 주간/일간 스낵 수는 created_date 범위로 집계하므로 별도 초기화가 필요 없다.
 */
@Entity
@Getter
@Table(
        name = "snack_history",
        indexes = {
                @Index(name = "idx_snack_history_created_date_user", columnList = "created_date, user_id"),
                @Index(name = "idx_snack_history_user_created_date", columnList = "user_id, created_date")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SnackHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "snack_history_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_snack_history_user")
    )
    private UserEntity user;

    private SnackHistory(UserEntity user) {
        this.user = user;
    }

    public static SnackHistory earnedBy(UserEntity user) {
        return new SnackHistory(user);
    }
}
