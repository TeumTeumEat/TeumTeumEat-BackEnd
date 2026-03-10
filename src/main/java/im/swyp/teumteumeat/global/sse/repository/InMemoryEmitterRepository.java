package im.swyp.teumteumeat.global.sse.repository;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class InMemoryEmitterRepository implements EmitterRepository {

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final Map<String, Object> eventCache = new ConcurrentHashMap<>();

    /* Emitter */
    @Override
    public void save(String id, SseEmitter emitter) {
        emitters.put(id, emitter);
    }

    @Override
    public Map<String, SseEmitter> findAllEmitterStartWithById(String id) {
        return findAllStartWithById(emitters, id);
    }

    @Override
    public void deleteById(String id) {
        emitters.remove(id);
    }

    /* Event Cache */
    @Override
    public void saveEventCache(String id, Object event) {
        eventCache.put(id, event);
    }

    @Override
    public Map<String, Object> findAllEventCacheStartWithById(String id) {
        return findAllStartWithById(eventCache, id);
    }

    /* HELPER METHOD */
    private <T> Map<String, T> findAllStartWithById(Map<String, T> map, String id) {
        return map.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(id))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
