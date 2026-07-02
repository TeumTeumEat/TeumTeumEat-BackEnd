package im.swyp.teumteumeat.domains.document.persistence.entity;

import im.swyp.teumteumeat.global.base.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
        name = "document_section",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"document_id", "total_sections", "section_index"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DocumentSection extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "document_section_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Column(name = "total_sections", nullable = false)
    private Integer totalSections;

    @Column(name = "section_index", nullable = false)
    private Integer sectionIndex;

    @Lob
    @Column(nullable = false)
    private String content;

    @Builder
    private DocumentSection(Document document, Integer totalSections, Integer sectionIndex, String content) {
        this.document = document;
        this.totalSections = totalSections;
        this.sectionIndex = sectionIndex;
        this.content = content;
    }
}
