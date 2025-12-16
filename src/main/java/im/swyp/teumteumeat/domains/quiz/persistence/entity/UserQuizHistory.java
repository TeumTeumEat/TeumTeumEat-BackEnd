package im.swyp.teumteumeat.domains.quiz.persistence.entity;

import im.swyp.teumteumeat.domains.user.persistence.entity.UserEntity;
import im.swyp.teumteumeat.global.base.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "user_quiz_history")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserQuizHistory extends BaseEntity { // 추후 퀴즈 복습 기능 및 스탬프에서 활용 가능

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Column(nullable = false)
    private boolean isCorrect;

    @Builder
    public UserQuizHistory(UserEntity user, Quiz quiz, boolean isCorrect) {
        this.user = user;
        this.quiz = quiz;
        this.isCorrect = isCorrect;
    }
}
