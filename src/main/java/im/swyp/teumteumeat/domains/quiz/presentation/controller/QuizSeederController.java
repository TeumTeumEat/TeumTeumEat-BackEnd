package im.swyp.teumteumeat.domains.quiz.presentation.controller;

import im.swyp.teumteumeat.domains.quiz.application.usecase.QuizSeederUseCase;
import im.swyp.teumteumeat.domains.quiz.presentation.api.QuizSeederApi;
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
public class QuizSeederController implements QuizSeederApi {

    private final QuizSeederUseCase quizSeederUseCase;

    @Override
    @PostMapping("/documents")
    public ResponseEntity<ApiResponse<String>> seedDocuments(
            @RequestParam Long startId,
            @RequestParam Long endId,
            @RequestParam(defaultValue = "1") int count) {

        int successCount = quizSeederUseCase.seedDocuments(startId, endId, count);
        return ResponseEntity.ok(
                ApiResponse.ofSuccess(CommonResponseCode.OK, "Document Seeding Completed. Created: " + successCount));
    }

    @Override
    @PostMapping("/quizzes")
    public ResponseEntity<ApiResponse<String>> seedQuizzes(
            @RequestParam Long startId,
            @RequestParam Long endId) {

        int successCount = quizSeederUseCase.seedQuizzes(startId, endId);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK,
                "Quiz Seeding Completed. Documents Processed: " + successCount));
    }
}
