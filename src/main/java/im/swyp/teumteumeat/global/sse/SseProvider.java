package im.swyp.teumteumeat.global.sse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;

/**
 * 실시간 알림(SSE)의 생성 및 전송을 담당하는 컴포넌트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SseProvider {

    private static final Long DEFAULT_TIMEOUT = 10 * 60 * 1000L; // 10MIN
    private static final String EVENT_NAME_CONNECT = "connect";
    private static final String MSG_CONNECT_SUCCESS = "EventStream Created.";
    private static final String ID_DELIMITER = ":";

    private final EmitterRepository emitterRepository;

    /**
     * 클라이언트와 서버 간의 SSE 연결을 생성하고 리포지토리에 저장
     *
     * @param key 연결 고유 Key (예: userId:docId)
     * @return 생성된 SseEmitter 객체
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
        sendToClient(emitter, emitterId, EVENT_NAME_CONNECT, MSG_CONNECT_SUCCESS);

        return emitter;
    }

    /**
     * 특정 키를 구독 중인 클라이언트에게 이벤트를 전송
     */
    public void sendEvent(String key, String eventName, Object data) {
        String searchPrefix = key + ID_DELIMITER;
        Map<String, SseEmitter> emitters = emitterRepository.findAllEmitterStartWithById(searchPrefix);
        emitters.forEach((id, emitter) -> {
            // 재연결을 위한 캐시 저장 (Last-Event-Id 사용)
            emitterRepository.saveEventCache(id, data);
            sendToClient(emitter, id, eventName, data);
        });
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
}
