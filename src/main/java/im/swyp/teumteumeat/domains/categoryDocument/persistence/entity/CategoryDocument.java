package im.swyp.teumteumeat.domains.categoryDocument.persistence.entity;

import im.swyp.teumteumeat.domains.category.persistence.entity.Category;
import im.swyp.teumteumeat.global.base.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "category_document")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CategoryDocument extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_document_id")
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Builder
    public CategoryDocument(String content, Category category) {
        this.content = content;
        this.category = category;
    }
}
