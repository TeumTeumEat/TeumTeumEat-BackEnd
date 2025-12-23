package im.swyp.teumteumeat.infra.s3.presentation.controller;

import im.swyp.teumteumeat.global.common.ApiResponse;
import im.swyp.teumteumeat.global.common.CommonResponseCode;
import im.swyp.teumteumeat.global.security.dto.CustomUserDetails;
import im.swyp.teumteumeat.infra.s3.application.dto.PresignedUrlRequest;
import im.swyp.teumteumeat.infra.s3.application.dto.PresignedUrlResponse;
import im.swyp.teumteumeat.infra.s3.application.usecase.S3UseCase;
import im.swyp.teumteumeat.infra.s3.presentation.api.S3Api;
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
public class S3Controller implements S3Api {

    private final S3UseCase s3UseCase;

    @Override
    @PostMapping("/presigned")
    public ResponseEntity<ApiResponse<PresignedUrlResponse>> getPresignedUrl(
            @RequestBody @Valid PresignedUrlRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        PresignedUrlResponse signedUrl = s3UseCase.generatePresignedUrl(request, user.getUserId());
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, signedUrl));
    }
}
