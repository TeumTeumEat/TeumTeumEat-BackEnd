package im.swyp.teumteumeat.domains.document.persistence.entity;

import im.swyp.teumteumeat.global.base.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "document_summary")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DocumentSummary extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "document_summary_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Column(length = 500)
    private String summary;

    @Column(length = 255)
    private String title;

    @Builder
    public DocumentSummary(Document document, String summary, String title) {
        this.document = document;
        this.summary = summary;
        this.title = title;
    }
}
