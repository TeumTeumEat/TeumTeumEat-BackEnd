package im.swyp.teumteumeat.domains.categoryDocument.domain.service;

import im.swyp.teumteumeat.domains.category.persistence.entity.Category;
import im.swyp.teumteumeat.domains.categoryDocument.domain.constant.CategoryDocumentResponseCode;
import im.swyp.teumteumeat.domains.categoryDocument.persistence.entity.CategoryDocument;
import im.swyp.teumteumeat.domains.categoryDocument.persistence.repository.CategoryDocumentRepository;
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
                .orElseThrow(() -> new BaseException(CategoryDocumentResponseCode.NOT_FOUND_CATEGORY_DOCUMENT));
    }

    @Transactional
    public CategoryDocument createDocument(Category category, String content, String title) {
        CategoryDocument document = CategoryDocument.builder()
                .category(category)
                .content(content)
                .title(title)
                .build();
        return categoryDocumentRepository.save(document);
    }

    @Transactional
    public void saveDocument(CategoryDocument document) {
        categoryDocumentRepository.save(document);
    }

    @Transactional
    public void deleteDocument(Long documentId) {
        categoryDocumentRepository.deleteById(documentId);
    }
}
