package im.swyp.teumteumeat.global.security.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import im.swyp.teumteumeat.global.security.constant.AuthResponseCode;
import im.swyp.teumteumeat.global.utils.ResponseUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * AuthenticationException(인증 예외) 처리
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    /**
     * 인증되지 않은 사용자(토큰 누락, 형식 오류 등)가 보호된 리소스에 접근 시 호출되는 메소드
     * 브라우저의 기본 동작(로그인 페이지 리다이렉트)을 방지
     */
    @Override
    public void commence(final HttpServletRequest request, final HttpServletResponse response, final AuthenticationException authException) throws IOException {
        log.warn("Unauthorized error: {} | Message: {} | Path: {}",
                request.getRemoteAddr(),
                authException.getMessage(),
                request.getRequestURI());

        log.debug("Authentication entry point exception details: ", authException);
        ResponseUtil.responseError(response, objectMapper, AuthResponseCode.UNAUTHORIZED);
    }
}