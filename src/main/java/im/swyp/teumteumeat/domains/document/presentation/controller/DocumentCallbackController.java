package im.swyp.teumteumeat.domains.document.presentation.controller;

import im.swyp.teumteumeat.domains.category.domain.constant.DocumentErrorType;
import im.swyp.teumteumeat.domains.document.application.dto.request.OcrInitRequest;
import im.swyp.teumteumeat.domains.document.application.dto.request.OcrPartRequest;
import im.swyp.teumteumeat.domains.document.application.usecase.DocumentUseCase;
import im.swyp.teumteumeat.domains.document.presentation.api.DocumentCallbackApi;
import im.swyp.teumteumeat.global.common.ApiResponse;
import im.swyp.teumteumeat.global.common.CommonResponseCode;
import im.swyp.teumteumeat.global.exception.BaseException;
import im.swyp.teumteumeat.global.security.constant.AuthResponseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/webhooks/ocr")
@RequiredArgsConstructor
public class DocumentCallbackController implements DocumentCallbackApi {

    @Value("${auth.internal-token}")
    private String internalToken;

    private final DocumentUseCase documentUseCase;

    @PostMapping("/init")
    public ResponseEntity<ApiResponse<Void>> init(
            @RequestHeader("X-INTERNAL-TOKEN") String token,
            @RequestBody OcrInitRequest request
    ) {
        validateToken(token);

        try {
            if (Boolean.FALSE.equals(request.success())) {
                documentUseCase.handleOcrFailure(request.fileKey(), request.reason());
                return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK));
            }
            documentUseCase.setParts(request);
        } catch (Exception e) {
            documentUseCase.handleOcrFailure(request.fileKey(), DocumentErrorType.SERVER_ERROR);
            throw e;
        }

        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK));
    }

    @PostMapping("/part")
    public ResponseEntity<ApiResponse<Void>> savePart(
            @RequestHeader("X-INTERNAL-TOKEN") String token,
            @RequestBody OcrPartRequest request
    ) {
        validateToken(token);

        try {
            if (Boolean.FALSE.equals(request.success())) {
                documentUseCase.handleOcrFailure(request.fileKey(), request.reason());
                return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK));
            }
            documentUseCase.saveParts(request);
        } catch (Exception e) {
            documentUseCase.handleOcrFailure(request.fileKey(), DocumentErrorType.SERVER_ERROR);
            throw e;
        }

        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK));
    }

    // 추후 Filter 등으로 리팩토링 가능
    private void validateToken(String token) {
        if (!internalToken.equals(token)) {
            throw new BaseException(AuthResponseCode.UNAUTHORIZED);
        }
    }
}