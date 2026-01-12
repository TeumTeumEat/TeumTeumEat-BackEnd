package im.swyp.teumteumeat.domains.document.application.mapper;

import im.swyp.teumteumeat.domains.document.application.dto.request.DocumentCreateRequest;
import im.swyp.teumteumeat.domains.document.application.dto.response.DocumentDetailResponse;
import im.swyp.teumteumeat.domains.document.application.dto.response.DocumentListResponse;
import im.swyp.teumteumeat.domains.document.application.dto.response.DocumentResponse;
import im.swyp.teumteumeat.domains.document.persistence.entity.Document;
import im.swyp.teumteumeat.domains.document.persistence.entity.DocumentSummary;
import im.swyp.teumteumeat.domains.goal.persistence.entity.Goal;
import im.swyp.teumteumeat.domains.user.persistence.entity.UserEntity;

import java.util.List;

import static im.swyp.teumteumeat.domains.document.domain.constant.FileStatus.PENDING;

public class DocumentMapper {
    public static Document toDocument(
            UserEntity user,
            Goal goal,
            DocumentCreateRequest request) {
        return Document.builder()
                .user(user)
                .goal(goal)
                .fileName(request.fileName())
                .fileKey(request.fileKey())
                .status(PENDING)
                .build();
    }

    public static Document toTempDocument(
            String fileKey,
            String fileName) {
        return Document.builder()
                .fileKey(fileKey)
                .fileName(fileName)
                .status(PENDING)
                .build();
    }

    public static DocumentResponse fromDocument(Document document) {
        return DocumentResponse.builder()
                .documentId(document.getId())
                .fileName(document.getFileName())
                .fileKey(document.getFileKey())
                .status(document.getStatus())
                .estimateTime(document.getEstimateTime())
                .build();
    }

    public static DocumentListResponse toDocumentListResponse(List<DocumentResponse> documents) {
        return DocumentListResponse.toDocumentListResponse(documents);
    }

    public static DocumentDetailResponse toDocumentDetailResponse(Document document, DocumentSummary documentSummary,
            boolean hasSolvedToday,
            boolean isFirstTime) {
        String summaryContent = (documentSummary != null) ? documentSummary.getSummary() : null;
        String title = (documentSummary != null && documentSummary.getTitle() != null) ? documentSummary.getTitle()
                : document.getTitle();
        return DocumentDetailResponse.builder()
                .documentId(document.getId())
                .fileName(document.getFileName())
                .fileKey(document.getFileKey())
                .title(title)
                .summary(summaryContent)
                .status(document.getStatus())
                .hasSolvedToday(hasSolvedToday)
                .isFirstTime(isFirstTime)
                .updatedAt(document.getUpdatedAt())
                .build();
    }
}
