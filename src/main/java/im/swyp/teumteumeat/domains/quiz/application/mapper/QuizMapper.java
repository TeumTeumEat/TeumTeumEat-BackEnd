package im.swyp.teumteumeat.domains.quiz.application.mapper;

import im.swyp.teumteumeat.domains.quiz.application.dto.response.QuizListResponse;
import im.swyp.teumteumeat.domains.quiz.persistence.entity.Quiz;
import im.swyp.teumteumeat.domains.userQuiz.application.dto.response.QuizSetResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class QuizMapper {

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

    private List<String> convertOptionsToList(String options) {
        if (options == null || options.isBlank()) {
            return List.of();
        }
        return List.of(options.split(","));
    }
}
