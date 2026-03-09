package im.swyp.teumteumeat.global.sse;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SseProvider sseProvider;

    public SseEmitter subscribe(String lastEventId, Runnable onCacheMiss, Object... identifiers) {
        String key = generateKey(identifiers);
        SseEmitter emitter = sseProvider.createEmitter(key);

        boolean recovered = false;
        // 재연결 시 누락된 데이터 복구
        if (lastEventId != null && !lastEventId.isEmpty()) {
            recovered = sseProvider.recoverEvents(emitter, key, lastEventId);
        }

        // 신규 구독이거나 누락된 데이터가 없다면 최신 상태를 반환
        if (!recovered) {
            onCacheMiss.run();
        }

        return emitter;
    }

    public void send(String key, String eventName, Object data) {
        sseProvider.sendEvent(key, eventName, data);
    }

    public String generateKey(Object... identifiers) {
        return sseProvider.generateKey(identifiers);
    }
}