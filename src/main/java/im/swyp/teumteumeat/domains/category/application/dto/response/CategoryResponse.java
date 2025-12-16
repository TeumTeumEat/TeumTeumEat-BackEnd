package im.swyp.teumteumeat.domains.category.application.dto.response;

import lombok.Builder;

@Builder
public record CategoryResponse(

        Long categoryId,

        String name,

        String path,

        String description
) {
}
