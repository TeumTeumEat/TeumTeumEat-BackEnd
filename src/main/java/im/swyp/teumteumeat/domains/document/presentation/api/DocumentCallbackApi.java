package im.swyp.teumteumeat.domains.document.presentation.api;

import im.swyp.teumteumeat.domains.document.application.dto.request.OcrInitRequest;
import im.swyp.teumteumeat.domains.document.application.dto.request.OcrPartRequest;
import im.swyp.teumteumeat.global.annotation.swagger.ApiErrorResponseExplanation;
import im.swyp.teumteumeat.global.annotation.swagger.ApiResponseExplanations;
import im.swyp.teumteumeat.global.annotation.swagger.ApiSuccessResponseExplanation;
import im.swyp.teumteumeat.global.common.ApiResponse;
import im.swyp.teumteumeat.global.security.constant.AuthResponseCode;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@Tag(name = "(백엔드 전용) Document Callback")
public interface DocumentCallbackApi {

    @ApiResponseExplanations(
            success = @ApiSuccessResponseExplanation(description = "처리 성공"),
            errors = {
                    @ApiErrorResponseExplanation(exceptionCode = AuthResponseCode.class, name = "UNAUTHORIZED")
            })
    ResponseEntity<ApiResponse<Void>> init(
            @RequestHeader("X-INTERNAL-TOKEN") String token,
            @RequestBody OcrInitRequest request
    );

    @ApiResponseExplanations(
            success = @ApiSuccessResponseExplanation(description = "처리 성공"),
            errors = {
                    @ApiErrorResponseExplanation(exceptionCode = AuthResponseCode.class, name = "UNAUTHORIZED")
            })
    ResponseEntity<ApiResponse<Void>> savePart(
            @RequestHeader("X-INTERNAL-TOKEN") String token,
            @RequestBody OcrPartRequest request
    );
}
