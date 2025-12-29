package im.swyp.teumteumeat.domains.document.application.mapper;

import im.swyp.teumteumeat.domains.document.persistence.entity.Document;
import im.swyp.teumteumeat.domains.document.persistence.entity.DocumentPart;

public class DocumentPartMapper {
    public static DocumentPart toDocumentPart(
            Document document,
            Integer partIndex,
            String ocrText
    ) {
        return DocumentPart.builder()
                .document(document)
                .partIndex(partIndex)
                .ocrText(ocrText)
                .build();
    }
}