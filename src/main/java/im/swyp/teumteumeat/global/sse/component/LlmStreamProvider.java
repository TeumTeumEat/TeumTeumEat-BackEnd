package im.swyp.teumteumeat.global.sse.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

// LLM 데이터 스트리밍(단발성)을 위한 SseEmitter 생성 컴포넌트
@Slf4j
@Component
public class LlmStreamProvider {

    private static final Long DEFAULT_STREAM_TIMEOUT = 10 * 60 * 1000L; // 10분
    private static final String EVENT_NAME_CONNECT = "connect";

    public SseEmitter createStreamEmitter() {
        return createStreamEmitter(DEFAULT_STREAM_TIMEOUT);
    }

    // 커스텀 타임아웃을 가진 스트림 전용 SseEmitter 생성 및 초기 CONNECT 이벤트 발송
    public SseEmitter createStreamEmitter(Long timeout) {
        SseEmitter emitter = new SseEmitter(timeout);
        try {
            // 프록시(Nginx 등) 503 에러 방지 및 연결 성공 알림을 위한 초기 이벤트
            emitter.send(SseEmitter.event().name(EVENT_NAME_CONNECT).data("Stream 연결"));
        } catch (IOException e) {
            log.error("스트리밍 연결 초기화 중 에러 발생", e);
            emitter.completeWithError(e);
        }
        return emitter;
    }
}
