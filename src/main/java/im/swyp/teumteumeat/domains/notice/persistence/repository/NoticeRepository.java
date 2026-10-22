package im.swyp.teumteumeat.domains.notice.persistence.repository;

import im.swyp.teumteumeat.domains.notice.persistence.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    Optional<Notice> findByIdAndIsDeletedFalse(Long id);

    Slice<Notice> findSliceByIsDeletedFalse(Pageable pageable);

    Page<Notice> findPageByIsDeletedFalse(Pageable pageable);

    @Query("SELECT n FROM Notice n WHERE n.isDeleted = false " +
            "AND n.popupStartAt <= :now AND n.popupEndAt > :now " +
            "ORDER BY n.id DESC")
    List<Notice> findPopupNotices(@Param("now") LocalDateTime now, Pageable pageable);
}
