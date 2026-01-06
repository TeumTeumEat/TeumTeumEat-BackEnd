package im.swyp.teumteumeat.global.config.notification;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import java.util.List;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "notification")
public class NotificationProperties {
    private final int streakThreshold;
    private final int streakMessageRatio;
    private final List<String> defaultMessages;
    private final List<String> streakMessages;

    // 설정값이 없을 경우 기본값 할당
    @ConstructorBinding
    public NotificationProperties(
            Integer streakThreshold,
            Integer streakMessageRatio,
            List<String> defaultMessages,
            List<String> streakMessages
    ) {
        this.streakThreshold = (streakThreshold != null) ? streakThreshold : 3;
        this.streakMessageRatio = (streakMessageRatio != null) ? streakMessageRatio : 5;
        this.defaultMessages = (defaultMessages != null) ? defaultMessages : List.of("학습할 시간이에요! 📚");
        this.streakMessages = (streakMessages != null) ? streakMessages : List.of("학습할 시간이에요! 📚");
    }
}