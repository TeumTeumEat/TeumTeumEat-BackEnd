package im.swyp.teumteumeat.domains.quiz.presentation.controller;

import im.swyp.teumteumeat.domains.category.persistence.repository.CategoryRepository;
import im.swyp.teumteumeat.domains.categoryDocument.domain.service.CategoryDocumentService;
import im.swyp.teumteumeat.domains.categoryDocument.persistence.entity.CategoryDocument;
import im.swyp.teumteumeat.domains.categoryDocument.persistence.repository.CategoryDocumentRepository;
import im.swyp.teumteumeat.domains.goal.persistence.entity.Goal;
import im.swyp.teumteumeat.domains.llm.domain.service.LLMService;
import im.swyp.teumteumeat.domains.quiz.application.usecase.QuizUseCase;
import im.swyp.teumteumeat.global.common.ApiResponse;
import im.swyp.teumteumeat.global.common.CommonResponseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/test/seed")
@RequiredArgsConstructor
@Slf4j
public class QuizSeederController {

    private final CategoryRepository categoryRepository;
    private final CategoryDocumentRepository categoryDocumentRepository;
    private final CategoryDocumentService categoryDocumentService;
    private final LLMService llmService;
    private final QuizUseCase quizUseCase;

    @PostMapping("/quizzes")
    public ResponseEntity<ApiResponse<String>> seedQuizzes(
            @RequestParam Long startId,
            @RequestParam Long endId) {

        log.info("Starting Quiz Seeding for categories {} to {}", startId, endId);
        int successCount = 0;

        for (long categoryId = startId; categoryId <= endId; categoryId++) {
            try {
                // 1. 카테고리 존재 확인
                if (!categoryRepository.existsById(categoryId)) {
                    log.warn("Category {} not found, skipping.", categoryId);
                    continue;
                }

                // 2. 카테고리 문서 확인 (없으면 생성)
                CategoryDocument document = categoryDocumentRepository.findTopByCategoryIdOrderByIdDesc(categoryId);

                if (document == null) {
                    log.info("Creating default document for Category {}", categoryId);
                    // 문서가 없으면 Goal도 없을 수 있음.
                    // 간단히 문서만 생성하기 위해 Category 정보 필요
                    var category = categoryRepository.findById(categoryId).get();
                    // 임시 제목 및 Summary 생성
                    String topic = "전반적인 " + category.getName() + " 개념";
                    String summary = llmService
                            .generateContent("Create a brief educational summary (around 500 chars) about "
                                    + category.getName() + " for a beginner developer.");

                    document = CategoryDocument.builder()
                            .category(category)
                            .title(topic)
                            .content(summary)
                            .goal(null) // Seeder로 만든 문서는 Goal 없음
                            .build();
                    categoryDocumentService.saveDocument(document);
                }

                // 3. 퀴즈 생성 (모든 난이도)
                quizUseCase.createDefaultQuizzesForCategoryDocument(document.getId());
                successCount++;
                log.info("Completed seeding for Category {}", categoryId);

            } catch (Exception e) {
                log.error("Failed to seed category {}", categoryId, e);
            }
        }

        return ResponseEntity
                .ok(ApiResponse.ofSuccess(CommonResponseCode.OK, "Seeding Completed. Count: " + successCount));
    }
}
