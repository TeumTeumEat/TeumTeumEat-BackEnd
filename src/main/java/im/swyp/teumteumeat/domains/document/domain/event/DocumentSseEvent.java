package im.swyp.teumteumeat.domains.document.domain.event;

import im.swyp.teumteumeat.domains.document.persistence.entity.Document;

public record DocumentSseEvent(
        Document document
) {
}
