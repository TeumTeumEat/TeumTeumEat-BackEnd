package im.swyp.teumteumeat.domains.document.domain.service;

import im.swyp.teumteumeat.domains.category.application.dto.response.DocumentStatusResponse;
import im.swyp.teumteumeat.domains.document.persistence.entity.Document;
import im.swyp.teumteumeat.global.sse.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DocumentNotificationService {

    private final NotificationService notificationService;

    private static final String SSE_EVENT_NAME = "document_processing_status";


    public void sendSseEvent(Document document) {
        // 임시 문서에 유저가 할당되지 않았다면 종료
        if (document.getUser() == null) {
            return;
        }

        String key = notificationService.generateKey(document.getUser().getId(), document.getId());
        notificationService.send(key, SSE_EVENT_NAME, DocumentStatusResponse.from(document));
    }
}