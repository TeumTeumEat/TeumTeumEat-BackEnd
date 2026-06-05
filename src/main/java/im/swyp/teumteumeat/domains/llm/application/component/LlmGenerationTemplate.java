package im.swyp.teumteumeat.domains.llm.application.component;

import im.swyp.teumteumeat.domains.llm.domain.service.LLMService;
import im.swyp.teumteumeat.global.sse.component.LlmStreamProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
@Slf4j
public class LlmGenerationTemplate {
    private final LLMService llmService;
    private final LlmStreamProvider llmStreamProvider;

    // TODO: 동기식 요약글 생성 로직

    // 비동기식 stream 요약글 생성 로직
    public <T> SseEmitter executeStream(String prompt, Function<String, T> saveAction,
                                        Function<T, String> titleExtractor,
                                        Consumer<T> postAction
                                        ) {
        SseEmitter sseEmitter = llmStreamProvider.createStreamEmitter(180_000L);
        StringBuilder generatedContent = new StringBuilder();

        // 한 글자씩 sse event로 전송
        try {
            llmService.generateContentStream(prompt)
                .subscribe(
                        parsedText -> {
                            try {
                                sseEmitter.send(SseEmitter.event().name("message").data(parsedText));
                                generatedContent.append(parsedText);
                            } catch (IOException e) {
                                sseEmitter.completeWithError(e);
                            }
                        },
                        error -> {
                            log.error("LLM Stream Error: ", error);
                            sseEmitter.completeWithError(error);
                        },
                        () -> {
                            // 비동기로 제목 생성 및 DB 저장 (스트리밍용 스레드 블로킹 방지)
                            CompletableFuture.supplyAsync(() ->
                                // 콜백
                                saveAction.apply(generatedContent.toString()))
                                        .thenAccept(title -> {
                                        try {
                                            // 제목 전송 (프론트엔드는 이 이벤트를 받아 UI의 제목 영역을 업데이트)
                                            sseEmitter.send(SseEmitter.event().name("title").data(title));
                                            // 모든 데이터 전송 완료 후 스트림 종료
                                            sseEmitter.complete();
                                        } catch (IOException e) {
                                            sseEmitter.completeWithError(e);
                                        }
                                    })
                                    .exceptionally(e -> {
                                        log.error("문서 저장 중 에러 발생!", e);
                                        sseEmitter.completeWithError(e);
                                        return null; // 에러 핸들링
                                    });
                        }

                );
    } catch (Exception e) {
        sseEmitter.completeWithError(e);
    }

        return sseEmitter;
    }
}
