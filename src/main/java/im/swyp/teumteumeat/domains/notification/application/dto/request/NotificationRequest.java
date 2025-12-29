package im.swyp.teumteumeat.domains.notification.application.dto.request;

import java.util.Map;

public record NotificationRequest(
        String title,
        String body,
        Map<String, String> data
) {
}
