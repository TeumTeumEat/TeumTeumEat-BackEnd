package im.swyp.teumteumeat.domains.notice.persistence.repository;

import im.swyp.teumteumeat.domains.notice.persistence.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    Optional<Notice> findByIdAndIsDeletedFalse(Long id);

    Slice<Notice> findSliceByIsDeletedFalse(Pageable pageable);

    Page<Notice> findPageByIsDeletedFalse(Pageable pageable);
}
