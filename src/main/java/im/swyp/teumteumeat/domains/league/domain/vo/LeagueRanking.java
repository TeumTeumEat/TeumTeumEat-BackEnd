package im.swyp.teumteumeat.domains.league.domain.vo;

import java.util.List;

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
}
