package im.swyp.teumteumeat.global.security.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import im.swyp.teumteumeat.global.common.CommonResponseCode;
import im.swyp.teumteumeat.global.utils.ResponseUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * AccessDeniedException(인가 예외) 처리
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    /**
     * 인증되었지만 해당 리소스에 접근 권한이 없는 경우 (403 Forbidden) 처리
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException {
        log.warn("AccessDeniedHandler: ", accessDeniedException);
        ResponseUtil.responseError(response, objectMapper, CommonResponseCode.FORBIDDEN);
    }
}