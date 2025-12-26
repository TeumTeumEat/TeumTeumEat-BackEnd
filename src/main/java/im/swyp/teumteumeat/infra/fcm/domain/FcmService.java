package im.swyp.teumteumeat.infra.fcm.domain;

import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

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
    public void sendNotification(String token, String title, String body, Map<String, String> data) {
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

        FirebaseMessaging.getInstance().sendAsync(message);
    }
}
