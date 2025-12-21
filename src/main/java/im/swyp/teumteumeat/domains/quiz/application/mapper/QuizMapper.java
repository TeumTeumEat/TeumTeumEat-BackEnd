package im.swyp.teumteumeat.domains.quiz.application.mapper;

import im.swyp.teumteumeat.domains.quiz.application.dto.response.QuizListResponse;
import im.swyp.teumteumeat.domains.quiz.persistence.entity.Quiz;
import im.swyp.teumteumeat.domains.userQuiz.application.dto.response.QuizSetResponse;
import org.springframework.stereotype.Component;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@Component
@RequiredArgsConstructor
public class QuizMapper {

    private final ObjectMapper objectMapper;

    public QuizListResponse.QuizDto toDto(Quiz quiz) {
        return QuizListResponse.QuizDto.builder()
                .quizId(quiz.getId())
                .question(quiz.getContent())
                .options(convertOptionsToList(quiz.getOptions()))
                .answer(quiz.getAnswer())
                .type(quiz.getQuizType().name())
                .explanation(quiz.getDescription())
                .build();
    }

    public QuizSetResponse toQuestionResponse(
            Quiz quiz) {
        return QuizSetResponse.builder()
                .quizId(quiz.getId())
                .question(quiz.getContent())
                .options(convertOptionsToList(quiz.getOptions()))
                .type(quiz.getQuizType().name())
                .build();
    }

    @SneakyThrows
    private List<String> convertOptionsToList(String options) {
        if (options == null || options.isBlank()) {
            return List.of();
        }
        if (options.trim().startsWith("[")) {
            try {
                return objectMapper.readValue(options, List.class);
            } catch (Exception e) {
                // JSON 파싱 실패 시, 혹시 모를 에러 로깅 후 fallback
                return List.of(options.split(","));
            }
        }
        return List.of(options.split(","));
    }
}
