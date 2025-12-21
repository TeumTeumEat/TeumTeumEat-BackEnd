package im.swyp.teumteumeat.infra.s3.presentation.api;

import im.swyp.teumteumeat.global.annotation.swagger.ApiResponseExplanations;
import im.swyp.teumteumeat.global.annotation.swagger.ApiSuccessResponseExplanation;
import im.swyp.teumteumeat.global.common.ApiResponse;
import im.swyp.teumteumeat.global.security.dto.CustomUserDetails;
import im.swyp.teumteumeat.infra.s3.application.dto.PresignedUrlRequest;
import im.swyp.teumteumeat.infra.s3.application.dto.PresignedUrlResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "S3", description = "Presigned Url 발급 API")
public interface S3Api {

    @Operation(
            summary = "Presigned Url 발급",
            description = "발급 후 presignedUrl을 PUT 요청으로 클라이언트에서 파일을 s3에 업로드한 후, fileName과 fileKey를 이용해 /api/v1/goals/{goalId}/documents 에 등록합니다."
    )
    @ApiResponseExplanations(
            success = @ApiSuccessResponseExplanation(
                    responseClass = PresignedUrlResponse.class,
                    description = "발급 성공"
            )
    )
    ResponseEntity<ApiResponse<PresignedUrlResponse>> getPresignedUrl(
            @RequestBody @Valid PresignedUrlRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user
    );
}
