package im.swyp.teumteumeat.domains.user.persistence.entity;

import im.swyp.teumteumeat.domains.document.persistence.entity.Document;
import im.swyp.teumteumeat.domains.goal.persistence.entity.Goal;
import im.swyp.teumteumeat.domains.notification.persistence.entity.DeviceToken;
import im.swyp.teumteumeat.domains.quiz.domain.constant.QuizResponseCode;
import im.swyp.teumteumeat.domains.user.application.dto.request.UserSettingsRequest;
import im.swyp.teumteumeat.domains.user.domain.constant.Role;
import im.swyp.teumteumeat.domains.userQuiz.persistence.entity.UserQuiz;
import im.swyp.teumteumeat.global.base.entity.BaseEntity;
import im.swyp.teumteumeat.global.exception.BaseException;
import im.swyp.teumteumeat.global.security.constant.SocialProvider;
import im.swyp.teumteumeat.global.utils.DatabaseEncryptionConverter;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

@Entity
@Getter
@DynamicUpdate
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_social", columnNames = {"social_provider", "social_id"})
        }
)
@Builder(access = AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    private String name;

    private String email;

    @Enumerated(EnumType.STRING)
    private SocialProvider socialProvider;

    private String socialId;

    @Convert(converter = DatabaseEncryptionConverter.class)
    private String socialRefreshToken;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "commute_info_id")
    private CommuteInfo commuteInfo;

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Goal> goals = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserQuiz> userQuizzes = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DeviceToken> deviceTokens = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Document> documents = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_goal_id")
    private Goal currentGoal;

    private boolean onboardingCompleted;

    private boolean pushEnabled;

    private boolean quizGuideSeen;

    @Builder.Default
    private int availableQuizCount = 1;

    @Column(name = "last_quiz_count_reset_date")
    private LocalDate lastQuizCountResetDate;

    @Builder.Default
    @Column(name = "daily_ad_reward_count", columnDefinition = "int default 0")
    private int dailyAdRewardCount = 0;

    public void resetAvailableQuizCountIfNeed() {
        LocalDate today = LocalDate.now();
        // 하루 초기화
        if (lastQuizCountResetDate == null || !lastQuizCountResetDate.isEqual(today)) {
            this.availableQuizCount = 1; // 풀이 가능한 하루 퀴즈 세트 수를 기본 값으로 리셋
            this.dailyAdRewardCount = 0; // 광고 보상 리셋
            this.lastQuizCountResetDate = today;
        }
    }

    public int getAvailableQuizCount() {
        resetAvailableQuizCountIfNeed();
        return this.availableQuizCount;
    }

    public boolean canSolveDailyQuiz() {
        return getAvailableQuizCount() > 0;
    }

    public void consumeQuizCount() {
        if (canSolveDailyQuiz()) {
            this.availableQuizCount--;
        }
    }

    public void addAvailableQuizCount(int count) {
        resetAvailableQuizCountIfNeed();
        this.availableQuizCount += count;
    }

    public void claimAdReward() {
        resetAvailableQuizCountIfNeed();
        if (this.dailyAdRewardCount >= 10) {
            throw new BaseException(
                QuizResponseCode.DAILY_AD_REWARD_LIMIT_EXCEEDED);
        }
        this.dailyAdRewardCount++;
        this.availableQuizCount++;
    }

    public void completeQuizGuide() {
        this.quizGuideSeen = true;
    }

    public void resetQuizGuide() {
        this.quizGuideSeen = false;
    }

    public static UserEntity socialSignup(String name, String email, SocialProvider socialProvider, String socialId) {
        return UserEntity.builder()
                .name(name)
                .email(email)
                .socialProvider(socialProvider)
                .socialId(socialId)
                .role(Role.USER)
                .pushEnabled(true)
                .build();
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updateCommuteInfo(CommuteInfo commuteInfo) {
        if (this.commuteInfo == null) {
            this.commuteInfo = commuteInfo;
        } else {
            this.commuteInfo.updateCommuteInfo(commuteInfo);
        }
    }

    public boolean updateAndGetOnboardingCompleted() {
        boolean onboardingCompleted = isOnboardingCompleted() ||
                name != null &&
                        commuteInfo != null &&
                        !goals.isEmpty();
        this.onboardingCompleted = onboardingCompleted;

        return onboardingCompleted;
    }

    public void updateSettings(UserSettingsRequest request) {
        if (request.pushEnabled() != null) {
            this.pushEnabled = request.pushEnabled();
        }
    }

    public void updateSocialRefreshToken(String socialRefreshToken) {
        this.socialRefreshToken = socialRefreshToken;
    }

    public void updateCurrentGoal(Goal goal) {
        this.currentGoal = goal;
    }
}