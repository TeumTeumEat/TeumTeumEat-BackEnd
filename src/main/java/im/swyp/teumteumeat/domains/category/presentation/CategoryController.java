package im.swyp.teumteumeat.domains.category.presentation;

import im.swyp.teumteumeat.domains.category.application.dto.request.CategoryRequest;
import im.swyp.teumteumeat.domains.category.application.dto.response.CategoryListResponse;
import im.swyp.teumteumeat.domains.category.application.usecase.CategoryUseCase;
import im.swyp.teumteumeat.global.common.ApiResponse;
import im.swyp.teumteumeat.global.common.CommonResponseCode;
import im.swyp.teumteumeat.global.security.dto.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryUseCase categoryUseCase;

    @GetMapping
    public ResponseEntity<ApiResponse<CategoryListResponse>> getCategories(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        CategoryListResponse categories = categoryUseCase.getCategories();
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, categories));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> createCategory(
            @RequestBody @Valid CategoryRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        categoryUseCase.createCategory(request);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK));
    }
}
