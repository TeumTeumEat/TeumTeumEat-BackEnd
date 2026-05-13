package im.swyp.teumteumeat.domains.category.application.usecase;

import im.swyp.teumteumeat.domains.category.application.dto.request.CategoryCreateRequest;
import im.swyp.teumteumeat.domains.category.application.dto.request.CategoryUpdateRequest;
import im.swyp.teumteumeat.domains.category.application.dto.response.CategoryListResponse;
import im.swyp.teumteumeat.domains.category.application.dto.response.CategoryResponse;
import im.swyp.teumteumeat.domains.category.application.mapper.CategoryMapper;
import im.swyp.teumteumeat.domains.category.domain.service.CategoryService;
import im.swyp.teumteumeat.domains.category.persistence.entity.Category;
import im.swyp.teumteumeat.global.annotation.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@UseCase
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryUseCase {

    private final CategoryService categoryService;

    public CategoryListResponse getCategories() {
        List<Category> categories = categoryService.getCategories();
        List<CategoryResponse> responses = categories.stream().map(CategoryMapper::fromCategory).toList();
        return CategoryMapper.toCategoryListResponse(responses);
    }

    @Transactional
    public void createCategory(CategoryCreateRequest request) {
        Category category = CategoryMapper.toCategory(request);
        categoryService.createCategory(category);
    }

    @Transactional
    public void updateCategory(Long categoryId, CategoryUpdateRequest request) {
        categoryService.updateCategory(categoryId, request);
    }

    @Transactional
    public void deleteCategory(Long categoryId) {
        categoryService.deleteCategory(categoryId);
    }
}
