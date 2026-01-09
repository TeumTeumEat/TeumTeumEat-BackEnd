package im.swyp.teumteumeat.domains.notification.domain.constant;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "notification")
public class NotificationProperties {
    private final int streakThreshold;
    private final int streakMessageRatio;
    private final List<String> defaultMessages;
    private final List<String> randomStreakMessages;
    private final List<FixedStreakMessage> fixedStreakMessages;

    @Data
    public static class FixedStreakMessage {
        private String message;
        private Integer date;
    }
}