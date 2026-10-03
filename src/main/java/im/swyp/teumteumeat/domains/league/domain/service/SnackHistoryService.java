package im.swyp.teumteumeat.domains.league.domain.service;

import im.swyp.teumteumeat.domains.league.persistence.entity.SnackHistory;
import im.swyp.teumteumeat.domains.league.persistence.repository.SnackHistoryRepository;
import im.swyp.teumteumeat.domains.user.persistence.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SnackHistoryService {

    private final SnackHistoryRepository snackHistoryRepository;

    /**
     * 퀴즈 세트 완료 시 스낵 1개 적립
     */
    @Transactional
    public void earnSnack(UserEntity user) {
        snackHistoryRepository.save(SnackHistory.earnedBy(user));
    }

    @Transactional
    public void deleteAllByUserId(Long userId) {
        snackHistoryRepository.deleteAllByUserId(userId);
    }
}
