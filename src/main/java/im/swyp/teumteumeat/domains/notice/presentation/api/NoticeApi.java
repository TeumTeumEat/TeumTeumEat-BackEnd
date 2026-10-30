package im.swyp.teumteumeat.domains.notice.presentation.api;

import im.swyp.teumteumeat.domains.notice.application.dto.request.NoticeCreateRequest;
import im.swyp.teumteumeat.domains.notice.application.dto.request.NoticeUpdateRequest;
import im.swyp.teumteumeat.domains.notice.application.dto.response.NoticePageResponse;
import im.swyp.teumteumeat.domains.notice.application.dto.response.NoticeSliceResponse;
import im.swyp.teumteumeat.domains.notice.domain.constant.NoticeResponseCode;
import im.swyp.teumteumeat.global.annotation.swagger.ApiErrorResponseExplanation;
import im.swyp.teumteumeat.global.annotation.swagger.ApiResponseExplanations;
import im.swyp.teumteumeat.global.annotation.swagger.ApiSuccessResponseExplanation;
import im.swyp.teumteumeat.global.annotation.swagger.PageableAsQueryParam;
import im.swyp.teumteumeat.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Notice", description = "공지사항 API")
public interface NoticeApi {

    @Operation(
            summary = "공지 목록 조회 (무한 스크롤)",
            description = "삭제되지 않은 공지를 최신순으로 조회합니다. 전체 개수 없이 다음 페이지 존재 여부(hasNext)만 반환합니다."
    )
    @PageableAsQueryParam
    @ApiResponseExplanations(
            success = @ApiSuccessResponseExplanation(
                    responseClass = NoticeSliceResponse.class,
                    description = "조회 성공"
            )
    )
    ResponseEntity<ApiResponse<NoticeSliceResponse>> getNotices(
            @Parameter(hidden = true) Pageable pageable
    );

    @Operation(
            summary = "(ADMIN) 공지 목록 조회 (페이지)",
            description = "삭제되지 않은 공지를 최신순으로 조회합니다. 전체 개수와 전체 페이지 수를 함께 반환합니다."
    )
    @PageableAsQueryParam
    @ApiResponseExplanations(
            success = @ApiSuccessResponseExplanation(
                    responseClass = NoticePageResponse.class,
                    description = "조회 성공"
            )
    )
    ResponseEntity<ApiResponse<NoticePageResponse>> getNoticesForAdmin(
            @Parameter(hidden = true) Pageable pageable
    );

    @Operation(
            summary = "(ADMIN) 공지 등록"
    )
    @ApiResponseExplanations(
            success = @ApiSuccessResponseExplanation(
                    description = "등록 성공"
            )
    )
    ResponseEntity<ApiResponse<Void>> createNotice(
            @RequestBody @Valid NoticeCreateRequest request
    );

    @Operation(
            summary = "(ADMIN) 공지 수정"
    )
    @ApiResponseExplanations(
            success = @ApiSuccessResponseExplanation(
                    description = "수정 성공"
            ),
            errors = {
                    @ApiErrorResponseExplanation(exceptionCode = NoticeResponseCode.class, name = "NOT_FOUND_NOTICE")
            }
    )
    ResponseEntity<ApiResponse<Void>> updateNotice(
            @PathVariable Long noticeId,
            @RequestBody @Valid NoticeUpdateRequest request
    );

    @Operation(
            summary = "(ADMIN) 공지 삭제"
    )
    @ApiResponseExplanations(
            success = @ApiSuccessResponseExplanation(
                    description = "삭제 성공"
            ),
            errors = {
                    @ApiErrorResponseExplanation(exceptionCode = NoticeResponseCode.class, name = "NOT_FOUND_NOTICE")
            }
    )
    ResponseEntity<ApiResponse<Void>> deleteNotice(
            @PathVariable Long noticeId
    );
}
