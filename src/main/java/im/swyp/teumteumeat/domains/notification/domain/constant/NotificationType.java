package im.swyp.teumteumeat.domains.notification.domain.constant;

/**
 * 푸시 알림 종류 (FCM data의 type 값으로 전달 -> 프론트 딥링크 연결)
 */
public enum NotificationType {
    LEAGUE_DEADLINE,
    LEAGUE_RESULT,
    ;

    public static final String DATA_KEY = "type";
}
