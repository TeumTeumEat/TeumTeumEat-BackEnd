package im.swyp.teumteumeat.domains.league.domain.vo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 주차별 리그 랭킹 (순위 순으로 정렬된 참여자 목록)
 * 모든 유저에게 동일하므로 Redis에 캐시해 공유한다.
 */
public record LeagueRanking(
        List<LeagueParticipant> participants
) {

    public static LeagueRanking empty() {
        return new LeagueRanking(List.of());
    }

    /**
     * 특정 유저의 순위 (1부터 시작, 해당 주 스낵이 0개라 랭킹에 없으면 empty)
     */
    public Optional<Integer> rankOf(Long userId) {
        for (int i = 0; i < participants.size(); i++) {
            if (participants.get(i).userId().equals(userId)) {
                return Optional.of(i + 1);
            }
        }
        return Optional.empty();
    }

    /**
     * 순위에 해당하는 참여자 (rankOf로 얻은 순위와 함께 사용)
     */
    public LeagueParticipant participantAt(int rank) {
        return participants.get(rank - 1);
    }

    /**
     * 유저 ID별 순위 (푸시 알림 등 여러 유저의 순위가 한꺼번에 필요할 때 사용)
     */
    public Map<Long, Integer> rankByUserId() {
        Map<Long, Integer> ranks = new HashMap<>();
        for (int i = 0; i < participants.size(); i++) {
            ranks.put(participants.get(i).userId(), i + 1);
        }
        return ranks;
    }
}
