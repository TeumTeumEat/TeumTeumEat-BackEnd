package im.swyp.teumteumeat.global.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import im.swyp.teumteumeat.global.exception.BaseException;
import im.swyp.teumteumeat.global.utils.ResponseUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtExceptionFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request,response);
        } catch (BaseException e) {
            ResponseUtil.responseError(response, objectMapper, e.getResponseCode());
        }
    }
}