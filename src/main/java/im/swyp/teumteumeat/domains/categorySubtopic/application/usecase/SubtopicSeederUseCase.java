package im.swyp.teumteumeat.domains.categorySubtopic.application.usecase;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import im.swyp.teumteumeat.domains.category.domain.service.CategoryService;
import im.swyp.teumteumeat.domains.category.persistence.entity.Category;
import im.swyp.teumteumeat.global.exception.BaseException;
import im.swyp.teumteumeat.domains.categorySubtopic.domain.service.CategorySubtopicService;
import im.swyp.teumteumeat.domains.categorySubtopic.persistence.entity.CategorySubtopic;
import im.swyp.teumteumeat.domains.llm.domain.prompt.DocumentPrompt;
import im.swyp.teumteumeat.domains.llm.domain.service.LLMService;
import im.swyp.teumteumeat.global.annotation.UseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@UseCase
@RequiredArgsConstructor
@Slf4j
public class SubtopicSeederUseCase {

    private final CategoryService categoryService;
    private final CategorySubtopicService subtopicService;
    private final LLMService llmService;
    private final ObjectMapper objectMapper;

    private static final int[] DURATION_WEEKS = {1, 2, 3, 4};

    public int seed(Long startId, Long endId, boolean overwrite) {
        int totalCount = 0;

        for (long categoryId = startId; categoryId <= endId; categoryId++) {
            Category category;
            try {
                category = categoryService.getCategoryById(categoryId);
            } catch (BaseException e) {
                log.warn("카테고리를 찾을 수 없어 건너뜁니다. ID: {}", categoryId);
                continue;
            }

            for (int weeks : DURATION_WEEKS) {
                if (!overwrite && subtopicService.hasSeed(categoryId, weeks)) {
                    log.info("스킵 (이미 존재): categoryId={}, {}주", categoryId, weeks);
                    continue;
                }
                try {
                    totalCount += generateAndSave(category, weeks);
                } catch (Exception e) {
                    log.error("서브주제 생성 실패: categoryId={}, {}주", categoryId, weeks, e);
                }
            }
        }

        return totalCount;
    }

    private int generateAndSave(Category category, int weeks) {
        int days = weeks * 7;
        String description = category.getDescription() != null
                ? category.getDescription()
                : category.getPath() + " " + category.getName();

        String prompt = String.format(
                DocumentPrompt.GENERATE_SUBTOPICS.getTemplate(),
                category.getName(), category.getPath(), description,
                weeks, days, days, days
        );

        String raw = llmService.generateContent(prompt);
        List<String> titles = parseSubtopics(raw, days);

        List<CategorySubtopic> subtopics = new ArrayList<>();
        for (int i = 0; i < titles.size(); i++) {
            subtopics.add(CategorySubtopic.builder()
                    .category(category)
                    .durationWeeks(weeks)
                    .sequenceIndex(i)
                    .title(titles.get(i))
                    .build());
        }

        subtopicService.saveAll(subtopics);
        log.info("서브주제 시딩 완료: categoryId={}, {}주, {}개", category.getId(), weeks, subtopics.size());
        return subtopics.size();
    }

    private List<String> parseSubtopics(String raw, int expectedCount) {
        try {
            // LLM 응답에서 JSON 블록만 추출 (마크다운 코드블록 등 제거)
            String json = extractJson(raw);
            JsonNode node = objectMapper.readTree(json);
            List<String> result = new ArrayList<>();
            node.get("subtopics").forEach(n -> result.add(n.asText()));

            if (result.size() != expectedCount) {
                log.warn("서브주제 개수 불일치: 기대={}, 실제={}", expectedCount, result.size());
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("서브주제 파싱 실패: " + raw, e);
        }
    }

    private String extractJson(String raw) {
        int start = raw.indexOf('{');
        int end = raw.lastIndexOf('}');
        if (start == -1 || end == -1 || start > end) {
            return raw.trim();
        }
        return raw.substring(start, end + 1);
    }
}
