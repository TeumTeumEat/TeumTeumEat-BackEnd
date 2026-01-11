package im.swyp.teumteumeat.domains.quiz.application.usecase;

import im.swyp.teumteumeat.domains.category.persistence.repository.CategoryRepository;
import im.swyp.teumteumeat.domains.categoryDocument.domain.service.CategoryDocumentService;
import im.swyp.teumteumeat.domains.categoryDocument.persistence.entity.CategoryDocument;
import im.swyp.teumteumeat.domains.categoryDocument.persistence.repository.CategoryDocumentRepository;
import im.swyp.teumteumeat.domains.llm.domain.service.LLMService;
import im.swyp.teumteumeat.global.annotation.UseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@UseCase
@RequiredArgsConstructor
@Slf4j
public class QuizSeederUseCase {

    private final CategoryRepository categoryRepository;
    private final CategoryDocumentRepository categoryDocumentRepository;
    private final CategoryDocumentService categoryDocumentService;
    private final LLMService llmService;
    private final QuizUseCase quizUseCase;

    @Transactional
    public int seedDocuments(Long startId, Long endId, int count) {
        log.info("문서 시딩 시작 - 카테고리 ID: {} ~ {}, 카테고리 당 문서 수: {}", startId, endId, count);
        int successCount = 0;

        for (long categoryId = startId; categoryId <= endId; categoryId++) {
            try {
                if (!categoryRepository.existsById(categoryId)) {
                    log.warn("카테고리를 찾을 수 없어 건너뜁니다. ID: {}", categoryId);
                    continue;
                }

                var category = categoryRepository.findById(categoryId).get();

                for (int i = 0; i < count; i++) {
                    String topic = "전반적인 " + category.getName() + " 개념 Part " + (i + 1);
                    String summary = llmService
                            .generateContent("Create a brief educational summary (around 500 chars) about "
                                    + category.getName() + " (Topic " + (i + 1) + ") for a beginner developer.");

                    CategoryDocument document = CategoryDocument.builder()
                            .category(category)
                            .title(topic)
                            .content(summary)
                            .goal(null)
                            .build();
                    categoryDocumentService.saveDocument(document);
                    successCount++;
                }
                log.info("카테고리 문서 시딩 완료 - ID: {}", categoryId);

            } catch (Exception e) {
                log.error("카테고리 문서 시딩 중 오류 발생 - ID: {}", categoryId, e);
            }
        }
        return successCount;
    }

    @Transactional
    public int seedQuizzes(Long startId, Long endId) {
        log.info("퀴즈 시딩 시작 - 카테고리 ID: {} ~ {}", startId, endId);
        int successCount = 0;

        for (long categoryId = startId; categoryId <= endId; categoryId++) {
            try {
                List<CategoryDocument> documents = categoryDocumentRepository
                        .findAllByCategoryIdAndGoalIsNull(categoryId);

                if (documents.isEmpty()) {
                    log.warn("해당 카테고리에 템플릿 문서가 없습니다. ID: {}", categoryId);
                    continue;
                }

                for (CategoryDocument doc : documents) {
                    quizUseCase.createDefaultQuizzesForCategoryDocument(doc.getId());
                    successCount++;
                }

                log.info("카테고리 퀴즈 시딩 완료 - ID: {}", categoryId);

            } catch (Exception e) {
                log.error("카테고리 퀴즈 시딩 중 오류 발생 - ID: {}", categoryId, e);
            }
        }
        return successCount;
    }
}
