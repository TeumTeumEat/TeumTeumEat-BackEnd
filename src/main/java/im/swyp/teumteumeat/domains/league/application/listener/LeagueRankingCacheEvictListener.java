package im.swyp.teumteumeat.domains.league.application.listener;

import im.swyp.teumteumeat.domains.league.application.component.LeagueRankingProvider;
import im.swyp.teumteumeat.domains.league.domain.event.SnackEarnedEvent;
import im.swyp.teumteumeat.domains.league.domain.vo.LeagueWeek;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class LeagueRankingCacheEvictListener {

    private final LeagueRankingProvider leagueRankingProvider;

    /**
     * 스낵 적립이 커밋된 뒤 적립 시각이 속한 주차의 랭킹 캐시를 삭제한다.
     * 커밋 전에 삭제하면 그 사이 조회가 적립 전 랭킹을 다시 캐시할 수 있으므로 AFTER_COMMIT에서 처리한다.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSnackEarnedEvent(SnackEarnedEvent event) {
        try {
            leagueRankingProvider.evict(LeagueWeek.of(event.earnedAt()));
        } catch (Exception e) {
            // 이미 커밋된 퀴즈 완료 요청을 실패로 만들지 않도록 로그만 남김 (캐시는 TTL로 만료됨)
            log.warn("Failed to evict league ranking cache. earnedAt={}", event.earnedAt(), e);
        }
    }
}
