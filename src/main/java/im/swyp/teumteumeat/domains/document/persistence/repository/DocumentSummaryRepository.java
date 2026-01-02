package im.swyp.teumteumeat.domains.document.persistence.repository;

import im.swyp.teumteumeat.domains.document.persistence.entity.DocumentSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DocumentSummaryRepository extends JpaRepository<DocumentSummary, Long> {

    // 문서의 최신 요약본 조회
    @Query("SELECT ds FROM DocumentSummary ds WHERE ds.document.id = :documentId ORDER BY ds.createdDate DESC LIMIT 1")
    Optional<DocumentSummary> findLatestByDocumentId(@Param("documentId") Long documentId);

    List<DocumentSummary> findAllByDocumentId(Long documentId);
}
