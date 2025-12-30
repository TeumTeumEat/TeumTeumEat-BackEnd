package im.swyp.teumteumeat.domains.document.application.mapper;

import im.swyp.teumteumeat.domains.document.application.dto.request.DocumentCreateRequest;
import im.swyp.teumteumeat.domains.document.application.dto.response.DocumentDetailResponse;
import im.swyp.teumteumeat.domains.document.application.dto.response.DocumentListResponse;
import im.swyp.teumteumeat.domains.document.application.dto.response.DocumentResponse;
import im.swyp.teumteumeat.domains.document.persistence.entity.Document;
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

    public static DocumentResponse fromDocument(Document document) {
        return DocumentResponse.builder()
                .documentId(document.getId())
                .fileName(document.getFileName())
                .fileKey(document.getFileKey())
                .status(document.getStatus())
                .build();
    }

    public static DocumentListResponse toDocumentListResponse(List<DocumentResponse> documents) {
        return DocumentListResponse.toDocumentListResponse(documents);
    }

    public static DocumentDetailResponse toDocumentDetailResponse(Document document) {
        return DocumentDetailResponse.builder()
                .documentId(document.getId())
                .fileName(document.getFileName())
                .fileKey(document.getFileKey())
                .summary(document.getSummary())
                .status(document.getStatus())
                .build();
    }
}
