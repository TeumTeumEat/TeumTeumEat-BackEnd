package im.swyp.teumteumeat.global.controller;

import im.swyp.teumteumeat.global.common.ApiResponse;
import im.swyp.teumteumeat.global.common.CommonResponseCode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.web.servlet.error.AbstractErrorController;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("${server.error.path:${error.path:/error}}")
public class CustomErrorController extends AbstractErrorController {

    public CustomErrorController(ErrorAttributes errorAttributes) {
        super(errorAttributes);
    }

    @RequestMapping
    public ResponseEntity<ApiResponse<Void>> handleError(HttpServletRequest request) {
        HttpStatus status = getStatus(request);
        CommonResponseCode responseCode = (status == HttpStatus.BAD_REQUEST)
                ? CommonResponseCode.BAD_REQUEST
                : CommonResponseCode.INTERNAL_SERVER_ERROR;

        return ResponseEntity
                .status(responseCode.getStatus())
                .body(ApiResponse.ofFail(responseCode));
    }
}
