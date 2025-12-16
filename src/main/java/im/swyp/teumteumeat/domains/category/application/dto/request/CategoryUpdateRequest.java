package im.swyp.teumteumeat.domains.category.application.dto.request;

public record CategoryUpdateRequest(

        String name,

        String path,

        String description
) {
}
