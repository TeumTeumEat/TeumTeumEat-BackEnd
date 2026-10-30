package im.swyp.teumteumeat.infra.fcm.dto;

import java.util.List;
import java.util.Map;

/**
 * 한 유저에게 보낼 푸시 알림 (FCM SDK 타입을 호출부에 노출하지 않기 위한 전송용 객체)
 * 유저의 모든 디바이스 토큰에 같은 내용으로 발송된다.
 *
 * @param tokens 수신 디바이스 토큰 목록
 * @param title  알림 제목
 * @param body   알림 본문
 * @param data   클라이언트 전달용 데이터 (없으면 빈 Map)
 */
public record PushMessage(
        List<String> tokens,
        String title,
        String body,
        Map<String, String> data
) {

    public PushMessage {
        tokens = List.copyOf(tokens);
        data = (data == null) ? Map.of() : Map.copyOf(data);
    }
}
