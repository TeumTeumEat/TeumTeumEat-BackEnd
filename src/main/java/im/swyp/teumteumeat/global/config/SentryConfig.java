package im.swyp.teumteumeat.global.config;

import im.swyp.teumteumeat.global.common.CommonResponseCode;
import im.swyp.teumteumeat.global.exception.BaseException;
import io.sentry.SentryOptions;
import org.springframework.beans.TypeMismatchException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.ErrorResponse;

@Configuration
public class SentryConfig {

    // 4xx은 클라이언트 요청 문제라 모두 노이즈로 보고 차단하고, 5xx만 전송
    // exception-resolver-order를 최우선으로 두어 GlobalExceptionHandler보다 먼저 예외를 캡처
    @Bean
    public SentryOptions.BeforeSendCallback sentryBeforeSendCallback() {
        return (event, hint) -> {
            HttpStatusCode status = resolveStatus(event.getThrowable());
            return status != null && status.is4xxClientError() ? null : event;
        };
    }

    // 캡처 시점엔 아직 응답 상태가 없으므로, GlobalExceptionHandler와 같은 기준으로 예외에서 상태코드를 추론
    private static HttpStatusCode resolveStatus(Throwable throwable) {
        if (throwable instanceof BaseException e) {
            return e.getResponseCode().getStatus();
        }
        // NoResourceFoundException(404) 등 Spring이 던지는 ErrorResponse 구현 예외
        if (throwable instanceof ErrorResponse e) {
            return e.getStatusCode();
        }
        // ErrorResponse 미구현이라 상태코드를 직접 못 읽고 GlobalExceptionHandler에서 고정 매핑하는 예외
        if (throwable instanceof AccessDeniedException) {
            return CommonResponseCode.FORBIDDEN.getStatus();
        }
        if (throwable instanceof HttpMessageNotReadableException) {
            return CommonResponseCode.BAD_REQUEST.getStatus();
        }
        if (throwable instanceof DataIntegrityViolationException) {
            return CommonResponseCode.DATA_INTEGRITY_VIOLATION.getStatus();
        }
        // ResponseEntityExceptionHandler 기본 구현이 400으로 응답
        if (throwable instanceof TypeMismatchException) {
            return HttpStatus.BAD_REQUEST;
        }
        return null;
    }
}
