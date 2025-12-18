package im.swyp.teumteumeat.domains.category.presentation.controller;

import im.swyp.teumteumeat.domains.category.application.dto.request.CategoryCreateRequest;
import im.swyp.teumteumeat.domains.category.application.dto.request.CategoryUpdateRequest;
import im.swyp.teumteumeat.domains.category.application.dto.response.CategoryListResponse;
import im.swyp.teumteumeat.domains.category.application.usecase.CategoryUseCase;
import im.swyp.teumteumeat.domains.category.presentation.api.CategoryApi;
import im.swyp.teumteumeat.global.common.ApiResponse;
import im.swyp.teumteumeat.global.common.CommonResponseCode;
import im.swyp.teumteumeat.global.security.dto.CustomUserDetails;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController implements CategoryApi {

    private final CategoryUseCase categoryUseCase;

    @Override
    @GetMapping
    public ResponseEntity<ApiResponse<CategoryListResponse>> getCategories(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        CategoryListResponse categories = categoryUseCase.getCategories();
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, categories));
    }

    @Override
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> createCategory(
            @RequestBody @Valid CategoryCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        categoryUseCase.createCategory(request);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK));
    }

    @Override
    @PatchMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> updateCategory(
            @NotNull Long categoryId,
            @RequestBody @Valid CategoryUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        categoryUseCase.updateCategory(categoryId, request);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK));
    }

    @Override
    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @NotNull Long categoryId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        categoryUseCase.deleteCategory(categoryId);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK));
    }
}
