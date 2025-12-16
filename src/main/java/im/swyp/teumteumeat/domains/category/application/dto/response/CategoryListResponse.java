package im.swyp.teumteumeat.domains.category.application.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record CategoryListResponse(

        List<CategoryResponse> categoryResponses
) {
    public static CategoryListResponse toCategoryListResponse(List<CategoryResponse> categories) {
        return CategoryListResponse.builder()
                .categoryResponses(categories)
                .build();
    }
}
