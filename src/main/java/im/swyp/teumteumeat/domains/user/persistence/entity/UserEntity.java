package im.swyp.teumteumeat.domains.user.persistence.entity;

import im.swyp.teumteumeat.domains.document.persistence.entity.Document;
import im.swyp.teumteumeat.domains.goal.persistence.entity.Goal;
import im.swyp.teumteumeat.domains.notification.persistence.entity.DeviceToken;
import im.swyp.teumteumeat.domains.user.application.dto.request.UserSettingsRequest;
import im.swyp.teumteumeat.domains.user.domain.constant.Role;
import im.swyp.teumteumeat.domains.userQuiz.persistence.entity.UserQuiz;
import im.swyp.teumteumeat.global.base.entity.BaseEntity;
import im.swyp.teumteumeat.global.security.constant.SocialProvider;
import im.swyp.teumteumeat.global.utils.DatabaseEncryptionConverter;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@DynamicUpdate
@Table(name = "users")
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