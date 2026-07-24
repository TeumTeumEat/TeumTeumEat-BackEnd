package im.swyp.teumteumeat.domains.document.domain.service;

import im.swyp.teumteumeat.domains.document.application.mapper.DocumentMapper;
import im.swyp.teumteumeat.domains.document.domain.constant.DocumentResponseCode;
import im.swyp.teumteumeat.domains.document.persistence.entity.Document;
import im.swyp.teumteumeat.domains.document.persistence.entity.DocumentPart;
import im.swyp.teumteumeat.domains.document.persistence.repository.DocumentPartRepository;
import im.swyp.teumteumeat.domains.document.persistence.repository.DocumentRepository;
import im.swyp.teumteumeat.global.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentPartRepository documentPartRepository;

    public Document getDocumentById(Long documentId) {
        return getOrThrow(documentId);
    }

    public Document getDocumentWithGoalAndUserById(Long documentId) {
        return documentRepository.findWithGoalAndUserById(documentId)
                .orElseThrow(() -> new BaseException(DocumentResponseCode.NOT_FOUND_DOCUMENT));
    }

    public Document getDocumentByFileKey(String fileKey) {
        return documentRepository.findByFileKey(fileKey)
                .orElseThrow(() -> new BaseException(DocumentResponseCode.NOT_FOUND_DOCUMENT));
    }

    public Optional<Document> getDocumnetByFileKeyOptional(String fileKey) {
        return documentRepository.findByFileKey(fileKey);
    }

    public List<Document> getDocumentsByGoalId(Long goalId) {
        return documentRepository.findAllByGoalId(goalId);
    }

    @Transactional
    public Document getOrSaveDocument(String fileKey, String fileName) {
        return documentRepository.findByFileKey(fileKey)
                .orElseGet(() -> {
                    Document tempDocument = DocumentMapper.toTempDocument(fileKey, fileName);
                    return documentRepository.save(tempDocument);
                });
    }

    @Transactional
    public void createDocument(Document document) {
        documentRepository.save(document);
    }

    @Transactional
    public void createDocumentPart(DocumentPart documentPart) {
        documentPartRepository.save(documentPart);
    }

    @Transactional
    public void deleteDocumentsByGoalId(Long goalId) {
        documentRepository.deleteAllByGoalId(goalId);
    }

    @Transactional
    public void deleteDocument(Long documentId) {
        documentRepository.deleteById(documentId);
    }

    /* HELPER METHOD */
    private Document getOrThrow(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new BaseException(DocumentResponseCode.NOT_FOUND_DOCUMENT));
    }
}
