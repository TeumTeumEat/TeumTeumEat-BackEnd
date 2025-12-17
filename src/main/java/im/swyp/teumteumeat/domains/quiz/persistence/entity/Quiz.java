package im.swyp.teumteumeat.domains.quiz.persistence.entity;

import im.swyp.teumteumeat.domains.categoryDocument.persistence.entity.CategoryDocument;
import im.swyp.teumteumeat.domains.quiz.domain.constant.QuizType;
import im.swyp.teumteumeat.global.base.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "quiz")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Quiz extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quiz_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    private QuizType quizType;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private String answer;

    @Column(nullable = false)
    private String description;

    @Column(columnDefinition = "TEXT")
    private String options; // 퀴즈 선지

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_document_id")
    private CategoryDocument categoryDocument;

    @Builder
    public Quiz(CategoryDocument categoryDocument, String content, String options, String answer, String description,
            QuizType quizType) {
        this.categoryDocument = categoryDocument;
        this.content = content;
        this.options = options;
        this.answer = answer;
        this.description = description;
        this.quizType = quizType;
    }

}
