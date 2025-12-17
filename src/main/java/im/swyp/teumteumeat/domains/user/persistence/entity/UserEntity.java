package im.swyp.teumteumeat.domains.user.persistence.entity;

import im.swyp.teumteumeat.domains.goal.persistence.entity.Goal;
import im.swyp.teumteumeat.domains.user.domain.constant.Role;
import im.swyp.teumteumeat.global.base.entity.BaseEntity;
import im.swyp.teumteumeat.global.security.constant.SocialProvider;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
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

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "commute_info_id")
    private CommuteInfo commuteInfo;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Goal> goals = new ArrayList<>();

    private boolean onboardingCompleted;

    public static UserEntity socialSignup(String name, String email, SocialProvider socialProvider, String socialId) {
        return UserEntity.builder()
                .name(name)
                .email(email)
                .socialProvider(socialProvider)
                .socialId(socialId)
                .role(Role.USER)
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

    public void changeOnboardingCompleted(boolean onboardingCompleted) {
        this.onboardingCompleted = onboardingCompleted;
    }

    public void updateRole(Role role) {
        this.role = role;
    }
}