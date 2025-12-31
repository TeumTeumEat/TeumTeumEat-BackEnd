package im.swyp.teumteumeat.domains.categoryDocument.application.dto.response;

import im.swyp.teumteumeat.domains.categoryDocument.persistence.entity.CategoryDocument;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record CategoryDocumentResponse(

        @Schema(description = "카테고리 자료(요약글) ID", example = "1") Long documentId,

        @Schema(description = "카테고리 요약글", example = "Spring은 자바 기반의 프레임워크로, ...") String content,

        @Schema(description = "오늘 퀴즈 풀이 여부", example = "true") boolean hasSolvedToday,

        @Schema(description = "최초 퀴즈 풀이 여부", example = "false") boolean isFirstTime) {
    public static CategoryDocumentResponse from(CategoryDocument document, boolean hasSolvedToday,
            boolean isFirstTime) {
        return CategoryDocumentResponse.builder()
                .documentId(document.getId())
                .content(document.getContent())
                .hasSolvedToday(hasSolvedToday)
                .isFirstTime(isFirstTime)
                .build();
    }
}
