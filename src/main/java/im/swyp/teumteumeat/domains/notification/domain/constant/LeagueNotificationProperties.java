package im.swyp.teumteumeat.domains.notification.domain.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "notification.league")
public class LeagueNotificationProperties {
    private final String title;
    private final String deadlineRankedMessage;
    private final String deadlineUnrankedMessage;
    private final String resultMessage;
}
