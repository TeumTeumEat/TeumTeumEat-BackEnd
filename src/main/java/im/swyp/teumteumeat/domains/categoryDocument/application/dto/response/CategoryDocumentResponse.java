package im.swyp.teumteumeat.domains.categoryDocument.application.dto.response;

import im.swyp.teumteumeat.domains.categoryDocument.persistence.entity.CategoryDocument;
import lombok.Builder;

@Builder
public record CategoryDocumentResponse(
        Long documentId,
        String content) {
    public static CategoryDocumentResponse from(CategoryDocument document) {
        return CategoryDocumentResponse.builder()
                .documentId(document.getId())
                .content(document.getContent())
                .build();
    }
}
