package im.swyp.teumteumeat.domains.league.domain.service;

import im.swyp.teumteumeat.domains.league.application.mapper.UserSnackCountMapping;
import im.swyp.teumteumeat.domains.league.domain.vo.LeagueWeek;
import im.swyp.teumteumeat.domains.league.persistence.entity.SnackHistory;
import im.swyp.teumteumeat.domains.league.persistence.repository.SnackHistoryRepository;
import im.swyp.teumteumeat.domains.user.persistence.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SnackHistoryService {

    private final SnackHistoryRepository snackHistoryRepository;

    /**
     * 퀴즈 세트 완료 시 스낵 1개 적립
     */
    @Transactional
    public SnackHistory earnSnack(UserEntity user) {
        return snackHistoryRepository.save(SnackHistory.earnedBy(user));
    }

    /**
     * 해당 주차에 스낵을 1개 이상 모은 유저별 주간/오늘 스낵 수
     */
    public List<UserSnackCountMapping> getSnackCountsByUser(LeagueWeek week) {
        return snackHistoryRepository.countSnacksByUser(week.weekStart(), week.nextWeekStart(), week.todayStart());
    }

    @Transactional
    public void deleteAllByUserId(Long userId) {
        snackHistoryRepository.deleteAllByUserId(userId);
    }
}
