package im.swyp.teumteumeat.global.sse.dto;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public record EmitterDto(String id, SseEmitter emitter) {}