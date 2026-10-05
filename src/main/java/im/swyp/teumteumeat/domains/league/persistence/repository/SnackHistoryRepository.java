package im.swyp.teumteumeat.domains.league.persistence.repository;

import im.swyp.teumteumeat.domains.league.application.mapper.UserSnackCountMapping;
import im.swyp.teumteumeat.domains.league.persistence.entity.SnackHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SnackHistoryRepository extends JpaRepository<SnackHistory, Long> {

    @Query("SELECT s.user.id AS userId, " +
            "COUNT(s) AS weeklySnackCount, " +
            "SUM(CASE WHEN s.createdDate >= :todayStart THEN 1 ELSE 0 END) AS todaySnackCount " +
            "FROM SnackHistory s " +
            "WHERE s.createdDate >= :weekStart AND s.createdDate < :nextWeekStart " +
            "GROUP BY s.user.id")
    List<UserSnackCountMapping> countSnacksByUser(
            @Param("weekStart") LocalDateTime weekStart,
            @Param("nextWeekStart") LocalDateTime nextWeekStart,
            @Param("todayStart") LocalDateTime todayStart);

    @Modifying(clearAutomatically = true)
    @Query("delete from SnackHistory s where s.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}
