package im.swyp.teumteumeat.domains.categorySubtopic.persistence.entity;

import im.swyp.teumteumeat.domains.category.persistence.entity.Category;
import im.swyp.teumteumeat.global.base.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
        name = "category_subtopic",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"category_id", "duration_weeks", "sequence_index"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CategorySubtopic extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subtopic_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "duration_weeks", nullable = false)
    private Integer durationWeeks;

    @Column(name = "sequence_index", nullable = false)
    private Integer sequenceIndex;

    @Column(nullable = false, length = 60)
    private String title;

    @Builder
    private CategorySubtopic(Category category, int durationWeeks, int sequenceIndex, String title) {
        this.category = category;
        this.durationWeeks = durationWeeks;
        this.sequenceIndex = sequenceIndex;
        this.title = title;
    }
}
