package im.swyp.teumteumeat.domains.user.persistence.entity;

import im.swyp.teumteumeat.domains.user.domain.constant.Role;
import im.swyp.teumteumeat.global.base.entity.BaseEntity;
import im.swyp.teumteumeat.global.security.constant.SocialProvider;
import jakarta.persistence.*;
import lombok.*;

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

    public static UserEntity socialSignup(String name, String email, SocialProvider socialProvider, String socialId) {
        return UserEntity.builder()
                .name(name)
                .email(email)
                .socialProvider(socialProvider)
                .socialId(socialId)
                .role(Role.USER)
                .build();
    }
}