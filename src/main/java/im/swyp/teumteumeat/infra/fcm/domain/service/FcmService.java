package im.swyp.teumteumeat.infra.fcm.domain.service;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.firebase.messaging.*;
import im.swyp.teumteumeat.domains.notification.domain.service.DeviceTokenService;
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

    public void sendBatchMessages(List<Message> messages, List<String> tokens, boolean dryRun) {
        // 500개씩 나누어 전송 (FCM 배치 제한)
        for (int i = 0; i < messages.size(); i += 500) {
            int toIndex = Math.min(i + 500, messages.size());
            List<Message> batchMessages = messages.subList(i, toIndex);
            List<String> batchTokens = new ArrayList<>(tokens.subList(i, toIndex)); // 토큰 리스트도 동일하게 쪼갬

            ApiFuture<BatchResponse> future = firebaseMessaging.sendEachAsync(batchMessages, dryRun);

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
