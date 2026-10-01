package im.swyp.teumteumeat.global.config;

import im.swyp.teumteumeat.domains.quiz.domain.constant.QuizResponseCode;
import im.swyp.teumteumeat.global.common.BaseResponseCode;
import im.swyp.teumteumeat.global.exception.BaseException;
import im.swyp.teumteumeat.global.security.constant.AuthResponseCode;
import io.sentry.SentryOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.ErrorResponse;

import java.util.Set;

@Configuration
public class SentryConfig {

    // 401(인증 만료) / 403(권한 없음) / 404(리소스 없음)만 흔한 노이즈로 보고 차단하고,
    // 그 외 4xx은 패턴 파악을 위해 그대로 전송
    private static final Set<Integer> IGNORED_STATUS_CODES = Set.of(
            HttpStatus.UNAUTHORIZED.value(),
            HttpStatus.FORBIDDEN.value(),
            HttpStatus.NOT_FOUND.value()
    );

    // exception-resolver-order를 최우선으로 두어 GlobalExceptionHandler보다 먼저 예외를 캡처
    @Bean
    public SentryOptions.BeforeSendCallback sentryBeforeSendCallback() {
        return (event, hint) -> {
            Throwable throwable = event.getThrowable();

            if (throwable instanceof BaseException baseException) {
                BaseResponseCode responseCode = baseException.getResponseCode();

                // GlobalExceptionHandler가 log.info로만 남기는, 상태코드와 무관한 의도된 정상 분기
                boolean isSilent = responseCode == AuthResponseCode.NEED_REGISTER
                        || responseCode == QuizResponseCode.TODAY_QUOTA_EXCEEDED;

                if (isSilent || IGNORED_STATUS_CODES.contains(responseCode.getStatus().value())) {
                    return null;
                }
            }

            // NoResourceFoundException(404) 등 Spring이 던지는 ErrorResponse 구현 예외
            if (throwable instanceof ErrorResponse errorResponse
                    && IGNORED_STATUS_CODES.contains(errorResponse.getStatusCode().value())) {
                return null;
            }

            // ErrorResponse 미구현이라 상태코드를 직접 못 읽는 403 고정 매핑 예외
            if (throwable instanceof AccessDeniedException) {
                return null;
            }

            return event;
        };
    }
}
