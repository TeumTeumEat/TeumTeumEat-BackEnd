package im.swyp.teumteumeat.domains.category.presentation.api;

import im.swyp.teumteumeat.domains.category.application.dto.request.CategoryCreateRequest;
import im.swyp.teumteumeat.domains.category.application.dto.request.CategoryUpdateRequest;
import im.swyp.teumteumeat.domains.category.application.dto.response.CategoryListResponse;
import im.swyp.teumteumeat.global.annotation.swagger.ApiResponseExplanations;
import im.swyp.teumteumeat.global.annotation.swagger.ApiSuccessResponseExplanation;
import im.swyp.teumteumeat.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Category", description = "카테고리 API")
public interface CategoryApi {

    @Operation(
            summary = "전체 카테고리 목록 조회"
    )
    @ApiResponseExplanations(
            success = @ApiSuccessResponseExplanation(
                    responseClass = CategoryListResponse.class,
                    description = "조회 성공"
            )
    )
    ResponseEntity<ApiResponse<CategoryListResponse>> getCategories(
    );

    @Operation(
            summary = "(ADMIN) 카테고리 추가"
    )
    @ApiResponseExplanations(
            success = @ApiSuccessResponseExplanation(
                    description = "추가 성공"
            )
    )
    ResponseEntity<ApiResponse<Void>> createCategory(
            @RequestBody @Valid CategoryCreateRequest request
    );

    @Operation(
            summary = "(ADMIN) 카테고리 수정"
    )
    @ApiResponseExplanations(
            success = @ApiSuccessResponseExplanation(
                    description = "수정 성공"
            )
    )
    ResponseEntity<ApiResponse<Void>> updateCategory(
            @NotNull Long categoryId,
            @RequestBody @Valid CategoryUpdateRequest request
    );

    @Operation(
            summary = "(ADMIN) 카테고리 삭제"
    )
    @ApiResponseExplanations(
            success = @ApiSuccessResponseExplanation(
                    description = "삭제 성공"
            )
    )
    ResponseEntity<ApiResponse<Void>> deleteCategory(
            @NotNull Long categoryId
    );
}
