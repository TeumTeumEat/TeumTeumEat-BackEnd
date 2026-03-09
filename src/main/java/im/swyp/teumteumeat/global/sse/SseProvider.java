package im.swyp.teumteumeat.global.sse;

import im.swyp.teumteumeat.global.sse.dto.SseConnectResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 실시간 알림(SSE)의 생성 및 전송을 담당하는 컴포넌트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SseProvider {

    private static final Long DEFAULT_TIMEOUT = 10 * 60 * 1000L; // 10MIN
    private static final String EVENT_NAME_CONNECT = "connect";
    private static final String ID_DELIMITER = ":";

    private final EmitterRepository emitterRepository;

    /**
     * 클라이언트와 서버 간의 SSE 연결을 생성하고 리포지토리에 저장
     */
    public SseEmitter createEmitter(String key) {
        String emitterId = makeTimeIncludeId(key);
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        emitterRepository.save(emitterId, emitter);

        emitter.onCompletion(() -> emitterRepository.deleteById(emitterId));
        emitter.onTimeout(() -> emitterRepository.deleteById(emitterId));
        emitter.onError((e) -> emitterRepository.deleteById(emitterId));

        // 503 에러를 방지하기 위한 더미 이벤트 전송
        // 연결이 이뤄진 후 하나의 데이터도 전송되지 않는다면, 유효 시간이 끝나면 503이 응답되는 문제가 있음.
        sendToClient(emitter, emitterId, EVENT_NAME_CONNECT, SseConnectResponse.connected());

        return emitter;
    }

    /**
     * 특정 키를 구독 중인 클라이언트에게 이벤트를 전송
     */
    public void sendEvent(String key, String eventName, Object data) {
        // 전송 시점의 ID 생성
        String eventId = key + ID_DELIMITER + System.currentTimeMillis();
        // 재연결을 위한 캐시 저장 (Last-Event-Id 사용)
        emitterRepository.saveEventCache(eventId, new SseEvent(eventName, data));

        // 현재 연결된 모든 emitter에 전송
        String searchPrefix = key + ID_DELIMITER;
        Map<String, SseEmitter> emitters = emitterRepository.findAllEmitterStartWithById(searchPrefix);
        emitters.forEach((emitterId, emitter) -> sendToClient(emitter, eventId, eventName, data));
    }

    /**
     * 재연결 시 캐시에서 찾아 미전송 데이터 전송
     */
    public void recoverEvents(SseEmitter target, String key, String lastEventId) {
        // 해당 유저의 모든 캐시 조회
        Map<String, Object> events = emitterRepository.findAllEventCacheStartWithById(key);
        // lastEventId보다 나중에 발생한 이벤트만 필터링하여 재전송
        events.entrySet().stream()
                .filter(entry -> lastEventId.compareTo(entry.getKey()) < 0)
                .forEach(entry -> {
                    SseEvent sseEvent = (SseEvent) entry.getValue();
                    sendToClient(target, entry.getKey(), sseEvent.name(), sseEvent.data());
                });
    }

    /**
     * 인자 목록을 기반으로 고유 Key 생성
     */
    public String generateKey(Object... identifiers) {
        return Arrays.stream(identifiers)
                .map(String::valueOf)
                .collect(Collectors.joining(ID_DELIMITER));
    }

    /* HELPER METHOD */
    // 실제 SseEmitter를 통해 데이터를 전송
    private void sendToClient(SseEmitter emitter, String id, String eventName, Object data) {
        try {
            emitter.send(SseEmitter.event()
                    .id(id)
                    .name(eventName)
                    .data(data)
            );
        } catch (IOException e) {
            emitterRepository.deleteById(id);
            log.warn("SSE Connection Disconnected. id: {}", id);
        }
    }

    private String makeTimeIncludeId(String key) {
        return key + ID_DELIMITER + System.currentTimeMillis();
    }

    private record SseEvent(String name, Object data) {}
}
