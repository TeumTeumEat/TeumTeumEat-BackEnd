package im.swyp.teumteumeat.domains.llm.application.component;

import im.swyp.teumteumeat.domains.llm.domain.service.LLMService;
import im.swyp.teumteumeat.global.common.CommonResponseCode;
import im.swyp.teumteumeat.global.component.DistributedLockFacade;
import im.swyp.teumteumeat.global.exception.BaseException;
import im.swyp.teumteumeat.global.sse.component.LlmStreamProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
@Slf4j
public class LlmGenerationTemplate {
    private final LLMService llmService;
    private final LlmStreamProvider llmStreamProvider;
    private final DistributedLockFacade distributedLockFacade;

    public <T> T executeSyncSummary(String lockKey, String prompt,
                                    Function<String, T> saveAction,
                                    Consumer<T> postAction) {
        return distributedLockFacade.tryExecuteWithLock(lockKey, 30, 60, TimeUnit.SECONDS,
                () -> {
                    String summaryContent = llmService.generateContent(prompt);
                    T savedEntity = saveAction.apply(summaryContent);

                    if (postAction != null) {
                        postAction.accept(savedEntity);
                    }
                    return savedEntity;
                }).orElseThrow(() -> new BaseException(CommonResponseCode.INTERNAL_SERVER_ERROR)
        );
    }


    // 비동기식 stream 요약글 생성 로직
    public <T> SseEmitter executeStreamSummary(String cooldownKey, String prompt, Function<String, T> saveAction,
                                               Function<T, String> titleExtractor,
                                               Consumer<T> postAction) {
        
        distributedLockFacade.checkAndSetCooldown(cooldownKey, 30);

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
                                distributedLockFacade.deleteCooldownKey(cooldownKey);
                                sseEmitter.completeWithError(e);
                            }
                        },
                        error -> {
                            log.error("LLM Stream Error: ", error);
                            distributedLockFacade.deleteCooldownKey(cooldownKey);
                            sseEmitter.completeWithError(error);
                        },
                        () -> {
                            // 비동기로 제목 생성 및 DB 저장 (스트리밍용 스레드 블로킹 방지)
                            CompletableFuture.supplyAsync(() ->
                                // 콜백
                                saveAction.apply(generatedContent.toString()))
                                    .thenAccept(savedContent -> {
                                            try {
                                                if (titleExtractor != null) {
                                                    String title = titleExtractor.apply(savedContent);
                                                    sseEmitter.send(SseEmitter.event().name("title").data(title));
                                                }

                                                // 모든 데이터 전송 완료 후 스트림 종료
                                                sseEmitter.complete();

                                                // (DocumentSummary 퀴즈 생성용) 후처리 콜백 비동기 실행
                                                if (postAction != null) {
                                                    CompletableFuture.runAsync(() -> postAction.accept(savedContent));
                                                }
                                            } catch (IOException e) {
                                                sseEmitter.completeWithError(e);
                                            } finally {
                                                distributedLockFacade.deleteCooldownKey(cooldownKey);
                                            }
                                    }).exceptionally(e -> {
                                            log.error("문서 저장 중 에러 발생!", e);
                                            distributedLockFacade.deleteCooldownKey(cooldownKey);
                                            sseEmitter.completeWithError(e);
                                            return null; // 에러 핸들링
                                    });
                        }

                );
    } catch (Exception e) {
        distributedLockFacade.deleteCooldownKey(cooldownKey);
        sseEmitter.completeWithError(e);
    }
        return sseEmitter;
    }
}
