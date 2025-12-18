package im.swyp.teumteumeat.domains.document.application.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record DocumentListResponse(

        List<DocumentResponse> documents
) {
    public static DocumentListResponse toDocumentListResponse(List<DocumentResponse> documents) {
        return DocumentListResponse.builder()
                .documents(documents)
                .build();
    }
}
