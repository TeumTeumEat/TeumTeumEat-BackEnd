package im.swyp.teumteumeat.infra.fcm.domain;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FcmService {

    private final FirebaseMessaging firebaseMessaging;

    public void sendBatchMessages(List<Message> messages, boolean dryRun) {
        // 500개씩 나누어 전송 (FCM 배치 제한)
        for (int i = 0; i < messages.size(); i += 500) {
            int toIndex = Math.min(i + 500, messages.size());
            List<Message> batch = messages.subList(i, toIndex);

            firebaseMessaging.sendEachAsync(batch, dryRun);
        }
    }
}
