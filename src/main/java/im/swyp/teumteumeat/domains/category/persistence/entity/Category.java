package im.swyp.teumteumeat.domains.category.persistence.entity;

import im.swyp.teumteumeat.global.base.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "category")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long id;

    private String name;

    private String path;

    private String description;

//    private List<Quiz> quizzes = new ArrayList<>();

    @Builder
    private Category(
            String name,
            String path,
            String description
    ) {
        this.name = name;
        this.path = path;
        this.description = description;
    }
}
