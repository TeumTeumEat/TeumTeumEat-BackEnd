package im.swyp.teumteumeat.domains.quiz.persistence.entity;

import im.swyp.teumteumeat.domains.llm.domain.constant.QuizType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Quiz {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Enum<QuizType> quizType;

    private String content;

    private int answer;

    private String description;
}
