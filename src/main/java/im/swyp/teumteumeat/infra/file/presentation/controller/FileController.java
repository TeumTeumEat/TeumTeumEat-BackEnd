package im.swyp.teumteumeat.infra.file.presentation.controller;

import im.swyp.teumteumeat.global.common.ApiResponse;
import im.swyp.teumteumeat.global.common.CommonResponseCode;
import im.swyp.teumteumeat.global.security.dto.CustomUserDetails;
import im.swyp.teumteumeat.infra.file.application.dto.PresignedUrlRequest;
import im.swyp.teumteumeat.infra.file.application.dto.PresignedUrlResponse;
import im.swyp.teumteumeat.infra.file.application.usecase.FileUseCase;
import im.swyp.teumteumeat.infra.file.presentation.api.FileApi;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/s3")
@RequiredArgsConstructor
public class FileController implements FileApi {

    private final FileUseCase fileUseCase;

    @Override
    @PostMapping("/presigned")
    public ResponseEntity<ApiResponse<PresignedUrlResponse>> getPresignedUrl(
            @RequestBody @Valid PresignedUrlRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        PresignedUrlResponse signedUrl = fileUseCase.generatePresignedUrl(request);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, signedUrl));
    }
}
