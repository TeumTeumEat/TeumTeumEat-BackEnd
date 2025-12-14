package im.swyp.teumteumeat.domains.user.persistence.entity;

import im.swyp.teumteumeat.global.base.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "commute_info")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommuteInfo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "commute_info_id")
    private Long id;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private int usageTime;

    @Builder
    private CommuteInfo(
            LocalDateTime startTime,
            LocalDateTime endTime,
            int usageTime
    ) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.usageTime = usageTime;
    }

    public void updateCommuteInfo(CommuteInfo commuteInfo) {
        this.startTime = commuteInfo.getStartTime();
        this.endTime = commuteInfo.getEndTime();
        this.usageTime = commuteInfo.getUsageTime();
    }
}
