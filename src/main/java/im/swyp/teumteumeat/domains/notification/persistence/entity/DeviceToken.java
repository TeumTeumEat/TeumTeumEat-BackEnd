package im.swyp.teumteumeat.domains.notification.persistence.entity;

import im.swyp.teumteumeat.domains.notification.domain.constant.DeviceType;
import im.swyp.teumteumeat.domains.user.persistence.entity.UserEntity;
import im.swyp.teumteumeat.global.base.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "device_token")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeviceToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "device_token_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DeviceType deviceType;

    @Builder
    private DeviceToken(UserEntity user, String token, DeviceType deviceType) {
        this.user = user;
        this.token = token;
        this.deviceType = deviceType;
    }
}
