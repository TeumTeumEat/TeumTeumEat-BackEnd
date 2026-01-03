package im.swyp.teumteumeat.infra.fcm.domain;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmService {

    /**
     * 푸쉬 알림 전송
     * @param token 디바이스 토큰
     * @param title 제목
     * @param body 내용
     * @param data 커스텀 데이터(클라이언트에서 처리)
     */
    @Async
    public void sendNotification(String token, String title, String body, Map<String, String> data) {
        try {
            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(
                            Notification.builder()
                                    .setTitle(title)
                                    .setBody(body)
                                    .build()
                    )
                    .putAllData(data)
                    .build();
            FirebaseMessaging.getInstance().send(message);
        } catch (Exception e) {
            log.error("Error sending notification", e);
        }
    }
}
