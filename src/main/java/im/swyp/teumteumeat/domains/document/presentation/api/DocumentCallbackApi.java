package im.swyp.teumteumeat.domains.document.presentation.api;

import im.swyp.teumteumeat.domains.document.application.dto.request.OcrInitRequest;
import im.swyp.teumteumeat.domains.document.application.dto.request.OcrPartRequest;
import im.swyp.teumteumeat.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@Tag(name = "(백엔드 전용) Document Callback")
public interface DocumentCallbackApi {

    ResponseEntity<ApiResponse<Void>> init(
            @RequestHeader("X-INTERNAL-TOKEN") String token,
            @RequestBody OcrInitRequest request
    );

    ResponseEntity<ApiResponse<Void>> savePart(
            @RequestHeader("X-INTERNAL-TOKEN") String token,
            @RequestBody OcrPartRequest request
    );
}
