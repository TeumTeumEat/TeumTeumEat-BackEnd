package im.swyp.teumteumeat.domains.categoryDocument.domain.service;

import im.swyp.teumteumeat.domains.categoryDocument.persistence.entity.CategoryDocument;
import im.swyp.teumteumeat.domains.categoryDocument.persistence.repository.CategoryDocumentRepository;
import im.swyp.teumteumeat.global.common.CommonResponseCode;
import im.swyp.teumteumeat.global.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryDocumentService {

    private final CategoryDocumentRepository categoryDocumentRepository;

    public List<CategoryDocument> getDocumentsByCategoryId(Long categoryId) {
        return categoryDocumentRepository.findAllByCategoryId(categoryId);
    }

    public CategoryDocument getDocumentById(Long documentId) {
        return categoryDocumentRepository.findById(documentId)
                .orElseThrow(() -> new BaseException(CommonResponseCode.NOT_FOUND));
    }

    @Transactional
    public void createDocument(CategoryDocument document) {
        categoryDocumentRepository.save(document);
    }

    @Transactional
    public void deleteDocument(Long documentId) {
        categoryDocumentRepository.deleteById(documentId);
    }
}
