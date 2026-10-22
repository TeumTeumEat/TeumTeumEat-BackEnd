package im.swyp.teumteumeat.infra.s3.presentation.api;

import im.swyp.teumteumeat.global.annotation.swagger.ApiErrorResponseExplanation;
import im.swyp.teumteumeat.global.annotation.swagger.ApiResponseExplanations;
import im.swyp.teumteumeat.global.annotation.swagger.ApiSuccessResponseExplanation;
import im.swyp.teumteumeat.global.common.ApiResponse;
import im.swyp.teumteumeat.global.security.annotation.LoginUser;
import im.swyp.teumteumeat.global.security.dto.CustomUserDetails;
import im.swyp.teumteumeat.infra.s3.application.dto.PresignedUrlRequest;
import im.swyp.teumteumeat.infra.s3.application.dto.PresignedUrlResponse;
import im.swyp.teumteumeat.infra.s3.constant.FileResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "S3", description = "Presigned Url 발급 API")
public interface S3Api {

    @Operation(
            summary = "Presigned Url 발급",
            description = """
                    발급 후 presignedUrl을 PUT 요청으로 클라이언트에서 파일을 s3에 업로드합니다.
                    - USER_PDF (기본값): pdf, 최대 50MB. 업로드 후 fileName과 key로 /api/v1/goals/{goalId}/documents 에 등록
                    - NOTICE_IMAGE (관리자 전용): png, jpg, jpeg, webp, 최대 10MB. 업로드 후 key를 공지 등록/수정의 fullScreenImageKey 또는 modalImageKey로 전달
                    """
    )
    @ApiResponseExplanations(
            success = @ApiSuccessResponseExplanation(
                    responseClass = PresignedUrlResponse.class,
                    description = "발급 성공"
            ),
            errors = {
                    @ApiErrorResponseExplanation(exceptionCode = FileResponseCode.class, name = "NOT_SUPPORTED_EXTENSION"),
                    @ApiErrorResponseExplanation(exceptionCode = FileResponseCode.class, name = "EXCEED_FILE_SIZE"),
                    @ApiErrorResponseExplanation(exceptionCode = FileResponseCode.class, name = "FORBIDDEN_FILE_TYPE")
            }
    )
    ResponseEntity<ApiResponse<PresignedUrlResponse>> getPresignedUrl(
            @RequestBody @Valid PresignedUrlRequest request,
            @Parameter(hidden = true) @LoginUser CustomUserDetails user
    );
}
