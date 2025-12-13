package im.swyp.teumteumeat.global.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import im.swyp.teumteumeat.global.common.ApiResponse;
import im.swyp.teumteumeat.global.common.BaseResponseCode;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;

import java.io.IOException;

import static im.swyp.teumteumeat.global.common.Constants.CHARACTER_ENCODING;

public class ResponseUtil {

    public static void responseError(HttpServletResponse response, ObjectMapper objectMapper, BaseResponseCode responseCode) throws IOException {
        response.setStatus(responseCode.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(CHARACTER_ENCODING);
        response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.ofFail(responseCode)));
    }
}