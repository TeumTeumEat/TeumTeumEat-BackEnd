package im.swyp.teumteumeat.infra.fcm.domain;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.firebase.messaging.*;
import im.swyp.teumteumeat.domains.notification.domain.service.DeviceTokenService;
import im.swyp.teumteumeat.infra.fcm.dto.PushMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmService {

    private final DeviceTokenService deviceTokenService;
    private final FirebaseMessaging firebaseMessaging;

    private static final int BATCH_SIZE = 500;

    /**
     * 푸시 알림을 디바이스 토큰별 FCM 메시지로 펼쳐 일괄 전송
     */
    public void send(List<PushMessage> pushMessages) {
        List<Message> messages = new ArrayList<>();
        List<String> tokens = new ArrayList<>();
        for (PushMessage pushMessage : pushMessages) {
            for (String token : pushMessage.tokens()) {
                messages.add(toMessage(token, pushMessage));
                tokens.add(token);
            }
        }

        sendBatchMessages(messages, tokens);
    }

    private Message toMessage(String token, PushMessage pushMessage) {
        return Message.builder()
                .setToken(token)
                .setNotification(Notification.builder()
                        .setTitle(pushMessage.title())
                        .setBody(pushMessage.body())
                        .build())
                .putAllData(pushMessage.data())
                .build();
    }

    private void sendBatchMessages(List<Message> messages, List<String> tokens) {
        // BATCH_SIZE개씩 나누어 전송 (FCM 배치 제한)
        for (int i = 0; i < messages.size(); i += BATCH_SIZE) {
            int toIndex = Math.min(i + BATCH_SIZE, messages.size());
            List<Message> batchMessages = messages.subList(i, toIndex);
            List<String> batchTokens = new ArrayList<>(tokens.subList(i, toIndex)); // 토큰 리스트도 동일하게 쪼갬

            ApiFuture<BatchResponse> future = firebaseMessaging.sendEachAsync(batchMessages);

            ApiFutures.addCallback(future, new ApiFutureCallback<>() {
                @Override
                public void onSuccess(BatchResponse response) {
                    if (response.getFailureCount() > 0) {
                        handleBatchFailures(batchTokens, response.getResponses());
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    // FCM 서버 연결 자체가 실패한 경우 (네트워크 장애 등)
                    log.error("FCM 배치 전송 중 시스템 레벨 에러 발생", t);
                }
            }, MoreExecutors.directExecutor());
        }
    }

    private void handleBatchFailures(List<String> originalTokens, List<SendResponse> responses) {
        List<String> tokensToDelete = new ArrayList<>();

        for (int i = 0; i < responses.size(); i++) {
            SendResponse res = responses.get(i);

            if (!res.isSuccessful()) {
                FirebaseMessagingException exception = res.getException();
                MessagingErrorCode errorCode = exception.getMessagingErrorCode();

                // 사용자가 앱을 삭제했거나 토큰이 유효하지 않은 경우
                if (errorCode == MessagingErrorCode.UNREGISTERED) {

                    String failedToken = originalTokens.get(i);
                    tokensToDelete.add(failedToken);
                }
            }
        }

        deviceTokenService.deleteInvalidTokens(tokensToDelete);
    }
}
