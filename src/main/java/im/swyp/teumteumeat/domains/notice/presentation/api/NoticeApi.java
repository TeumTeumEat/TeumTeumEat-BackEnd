package im.swyp.teumteumeat.domains.notice.presentation.api;

import im.swyp.teumteumeat.domains.notice.application.dto.request.NoticeCreateRequest;
import im.swyp.teumteumeat.domains.notice.application.dto.request.NoticeUpdateRequest;
import im.swyp.teumteumeat.domains.notice.application.dto.response.NoticeModalListResponse;
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

    String IMAGE_QUERY_PARAMETER_DESCRIPTION = """

            #### 이미지 URL 쿼리 파라미터
            이미지 URL 뒤에 쿼리 파라미터를 붙이면 리사이즈/포맷 변환된 이미지를 받을 수 있습니다. (예: `{imageUrl}?w=720&f=webp&q=80`)

            | 파라미터 | 값 | 설명 |
            |---|---|---|
            | `w` | 1 ~ 4096 | 너비(px) |
            | `h` | 1 ~ 4096 | 높이(px) |
            | `f` | `webp` | 포맷 변환. **webp만 지원**하며, 생략하면 원본 포맷을 유지 |
            | `q` | 1 ~ 100 | 품질. 생략하면 80 |
            """;

    @Operation(
            summary = "공지 목록 조회 (무한 스크롤)",
            description = "공지를 최신순으로 조회합니다. 첫 요청은 lastNoticeId 없이 보내고, 다음 요청부터는 직전 응답의 마지막 공지 noticeId를 lastNoticeId로 보냅니다. hasNext가 false이면 더 조회할 공지가 없습니다. 공지를 누르면 detail 값으로 전체화면을 띄웁니다. detail.externalLink가 있으면 외부 URL, detail.deepLink가 있으면 앱 내부 딥링크로 이동하고, 둘 다 null이면 액션 버튼을 띄우지 않습니다."
                    + IMAGE_QUERY_PARAMETER_DESCRIPTION
    )
    @ApiResponseExplanations(
            success = @ApiSuccessResponseExplanation(
                    responseClass = NoticeSliceResponse.class,
                    description = "조회 성공"
            )
    )
    ResponseEntity<ApiResponse<NoticeSliceResponse>> getNotices(
            @Parameter(description = "직전 응답의 마지막 공지 ID, 첫 요청은 생략", example = "42") Long lastNoticeId,
            @Parameter(description = "조회할 공지 수 (1 ~ 50, 범위를 벗어나면 가까운 값으로 보정)", example = "20") int size
    );

    @Operation(
            summary = "메인 모달 공지 조회",
            description = "메인 화면 진입 시 띄울 모달로, 팝업 기간 내 공지를 최신순 리스트로 조회합니다. 현재는 최대 1개만 내려가지만 추후 N개로 늘어날 수 있으니 리스트 순서대로 처리해 주세요. 공지가 없으면 notices가 빈 배열입니다. 모달을 누르면 추가 조회 없이 detail로 전체화면을 띄웁니다. 액션 버튼 규칙은 공지 목록 조회와 같습니다."
                    + IMAGE_QUERY_PARAMETER_DESCRIPTION
    )
    @ApiResponseExplanations(
            success = @ApiSuccessResponseExplanation(
                    responseClass = NoticeModalListResponse.class,
                    description = "조회 성공"
            )
    )
    ResponseEntity<ApiResponse<NoticeModalListResponse>> getNoticeModals();

    @Operation(
            summary = "(ADMIN) 공지 목록 조회 (페이지)",
            description = "공지를 최신순으로 조회합니다. 전체 개수와 전체 페이지 수를 함께 반환합니다."
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
