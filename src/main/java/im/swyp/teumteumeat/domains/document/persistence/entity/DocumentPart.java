package im.swyp.teumteumeat.domains.document.persistence.entity;

import im.swyp.teumteumeat.global.base.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "document_part")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DocumentPart extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "document_part_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    private Integer partIndex;

    @Lob
    private String ocrText;

    @Builder
    private DocumentPart(
            Document document,
            Integer partIndex,
            String ocrText
    ) {
        this.document = document;
        this.partIndex = partIndex;
        this.ocrText = ocrText;
    }
}