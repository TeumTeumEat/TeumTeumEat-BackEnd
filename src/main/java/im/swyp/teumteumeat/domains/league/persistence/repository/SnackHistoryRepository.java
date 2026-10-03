package im.swyp.teumteumeat.domains.league.persistence.repository;

import im.swyp.teumteumeat.domains.league.persistence.entity.SnackHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SnackHistoryRepository extends JpaRepository<SnackHistory, Long> {

    @Modifying(clearAutomatically = true)
    @Query("delete from SnackHistory s where s.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}
