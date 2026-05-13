package im.swyp.teumteumeat.global.sse.service;

import im.swyp.teumteumeat.global.sse.component.SseProvider;
import im.swyp.teumteumeat.global.sse.dto.EmitterDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SseProvider sseProvider;

    public SseEmitter subscribe(String lastEventId, Consumer<EmitterDto> onCacheMiss, Object... identifiers) {
        String key = generateKey(identifiers);
        EmitterDto dto = sseProvider.createEmitter(key);
        String emitterId = dto.id();
        SseEmitter emitter = dto.emitter();

        boolean recovered = false;
        // 재연결 시 누락된 데이터 복구
        if (lastEventId != null && !lastEventId.isEmpty()) {
            recovered = sseProvider.recoverEvents(emitter, emitterId, key, lastEventId);
        }

        // 신규 구독이거나 누락된 데이터가 없다면 최신 상태를 반환
        if (!recovered) {
            onCacheMiss.accept(dto);
        }

        return emitter;
    }

    public void send(String key, String eventName, Object data) {
        sseProvider.sendEvent(key, eventName, data);
    }

    public void sendToTarget(SseEmitter target, String emitterId, String eventId, String eventName, Object data) {
        sseProvider.sendToClient(target, emitterId, eventId, eventName, data);
    }

    public String generateKey(Object... identifiers) {
        return sseProvider.generateKey(identifiers);
    }
}