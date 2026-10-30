package im.swyp.teumteumeat.domains.league.domain.event;

import java.time.LocalDateTime;

/**
 * 스낵 적립 이벤트 (커밋 후 해당 주차 리그 랭킹 캐시 삭제용)
 *
 * @param earnedAt 스낵 적립 시각 (적립 기록의 created_date)
 */
public record SnackEarnedEvent(
        LocalDateTime earnedAt
) {
}
