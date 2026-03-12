package im.swyp.teumteumeat.global.sse.repository;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.util.Map;

public interface EmitterRepository {
    /* Emitter */
    void save(String id, SseEmitter emitter);
    
    // 특정 식별자로 시작하는 모든 Emitter 조회
    Map<String, SseEmitter> findAllEmitterStartWithById(String id);
    
    void deleteById(String id);

    /* Event Cache */
    // 전송한 이벤트를 캐시에 저장
    void saveEventCache(String id, Object event);

    // 특정 식별자로 시작하는 모든 이벤트 캐시 조회 (유실 데이터 조회용)
    Map<String, Object> findAllEventCacheStartWithById(String id);
}