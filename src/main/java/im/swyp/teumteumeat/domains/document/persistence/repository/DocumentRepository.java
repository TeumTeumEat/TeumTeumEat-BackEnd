package im.swyp.teumteumeat.domains.document.persistence.repository;

import im.swyp.teumteumeat.domains.document.persistence.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findAllByGoalId(Long goalId);
    void deleteAllByGoalId(Long goalId);
    Optional<Document> findByFileKey(String fileKey);
}
