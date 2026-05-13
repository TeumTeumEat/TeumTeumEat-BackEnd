package im.swyp.teumteumeat.domains.user.persistence.entity;

import im.swyp.teumteumeat.global.base.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Entity
@Getter
@Table(name = "commute_info", indexes = {
        @Index(name = "idx_commute_start", columnList = "start_Time"),
        @Index(name = "idx_commute_end", columnList = "end_Time")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommuteInfo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "commute_info_id")
    private Long id;

    private LocalTime startTime;

    private LocalTime endTime;

    private int usageTime;

    @Builder
    private CommuteInfo(
            LocalTime startTime,
            LocalTime endTime,
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
