package im.swyp.teumteumeat.domains.document.persistence.repository;

import im.swyp.teumteumeat.domains.document.persistence.entity.DocumentSection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentSectionRepository extends JpaRepository<DocumentSection, Long> {

    List<DocumentSection> findByDocumentIdAndTotalSectionsOrderBySectionIndex(Long documentId, Integer totalSections);
}
