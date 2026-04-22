package im.swyp.teumteumeat.domains.common.history.application.mapper;

import im.swyp.teumteumeat.domains.common.history.application.dto.response.HistoryQuizListResponse;
import im.swyp.teumteumeat.domains.quiz.application.dto.response.QuizListResponse;
import im.swyp.teumteumeat.domains.quiz.application.mapper.QuizMapper;
import im.swyp.teumteumeat.domains.quiz.persistence.entity.Quiz;
import im.swyp.teumteumeat.domains.userQuiz.persistence.entity.UserQuiz;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HistoryMapper {

    private final QuizMapper quizMapper;

    public HistoryQuizListResponse.HistoryQuizDto toDto(UserQuiz userQuiz) {
        Quiz quiz = userQuiz.getQuiz();
        QuizListResponse.QuizDto baseDto = quizMapper.toDto(quiz);

        return HistoryQuizListResponse.HistoryQuizDto.builder()
                .quizId(baseDto.quizId())
                .question(baseDto.question())
                .options(baseDto.options())
                .answer(baseDto.answer())
                .type(baseDto.type())
                .explanation(baseDto.explanation())
                .isCorrect(userQuiz.isCorrect())
                .build();
    }
}
