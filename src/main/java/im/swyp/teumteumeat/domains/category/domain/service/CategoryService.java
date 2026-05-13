package im.swyp.teumteumeat.domains.category.domain.service;

import im.swyp.teumteumeat.domains.category.application.dto.request.CategoryUpdateRequest;
import im.swyp.teumteumeat.domains.category.domain.constant.CategoryResponseCode;
import im.swyp.teumteumeat.domains.category.persistence.entity.Category;
import im.swyp.teumteumeat.domains.category.persistence.repository.CategoryRepository;
import im.swyp.teumteumeat.global.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public Category getCategoryById(Long categoryId) {
        return getOrThrow(categoryId);
    }

    public List<Category> getCategories() {
        return categoryRepository.findAll();
    }

    public void createCategory(Category category) {
        categoryRepository.save(category);
    }

    public void updateCategory(Long categoryId, CategoryUpdateRequest request) {
        Category category = getCategoryById(categoryId);
        category.updateCategory(
                request.name(),
                request.path(),
                request.description()
        );
    }

    public void deleteCategory(Long categoryId) {
        categoryRepository.deleteById(categoryId);
    }

    /* HELPER METHOD */
    private Category getOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new BaseException(CategoryResponseCode.NOT_FOUND_CATEGORY));
    }
}
