package im.swyp.teumteumeat.domains.document.persistence.repository;

import im.swyp.teumteumeat.domains.document.persistence.entity.DocumentPart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentPartRepository extends JpaRepository<DocumentPart, Long> {
}