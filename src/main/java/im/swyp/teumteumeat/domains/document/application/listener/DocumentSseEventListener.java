package im.swyp.teumteumeat.domains.document.application.listener;

import im.swyp.teumteumeat.domains.document.domain.event.DocumentSseEvent;
import im.swyp.teumteumeat.domains.document.domain.service.DocumentNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class DocumentSseEventListener {

    private final DocumentNotificationService documentNotificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleDocumentSseEvent(DocumentSseEvent event) {
        documentNotificationService.sendSseEvent(event.document());
    }
}