package im.swyp.teumteumeat.domains.document.domain.service;

import im.swyp.teumteumeat.domains.document.persistence.entity.Document;
import im.swyp.teumteumeat.domains.document.persistence.repository.DocumentRepository;
import im.swyp.teumteumeat.global.common.CommonResponseCode;
import im.swyp.teumteumeat.global.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;

    public Document getDocumentById(Long documentId) {
        return getOrThrow(documentId);
    }

    public List<Document> getDocumentsByGoalId(Long goalId) {
        return documentRepository.findAllByGoalId(goalId);
    }

    public void createDocument(Document document) {
        documentRepository.save(document);
    }

    public void deleteDocumentsByGoalId(Long goalId) {
        documentRepository.deleteAllByGoalId(goalId);
    }

    public void deleteDocument(Long documentId) {
        documentRepository.deleteById(documentId);
    }

    /* HELPER METHOD */
    private Document getOrThrow(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new BaseException(CommonResponseCode.NOT_FOUND));
    }
}
