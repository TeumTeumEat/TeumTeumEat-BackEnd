package im.swyp.teumteumeat.domains.category.application.mapper;

import im.swyp.teumteumeat.domains.category.application.dto.request.CategoryCreateRequest;
import im.swyp.teumteumeat.domains.category.application.dto.response.CategoryListResponse;
import im.swyp.teumteumeat.domains.category.application.dto.response.CategoryResponse;
import im.swyp.teumteumeat.domains.category.persistence.entity.Category;

import java.util.List;

public class CategoryMapper {
    public static Category toCategory(
            CategoryCreateRequest request
    ) {
        return Category.builder()
                .name(request.name())
                .path(request.path())
                .description(request.description())
                .build();
    }

    public static CategoryResponse fromCategory(Category category) {
        return CategoryResponse.builder()
                .categoryId(category.getId())
                .name(category.getName())
                .path(category.getPath())
                .description(category.getDescription())
                .build();
    }

    public static CategoryListResponse toCategoryListResponse(List<CategoryResponse> categories) {
        return CategoryListResponse.toCategoryListResponse(categories);
    }

}
