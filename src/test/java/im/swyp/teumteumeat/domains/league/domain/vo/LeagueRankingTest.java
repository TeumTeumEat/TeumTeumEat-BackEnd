package im.swyp.teumteumeat.domains.league.domain.vo;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LeagueRankingTest {

    private final LeagueRanking ranking = new LeagueRanking(List.of(
            participant(30L, 10),
            participant(10L, 7),
            participant(20L, 3)));

    @Test
    void 정렬된_목록의_위치로_순위를_반환한다() {
        assertThat(ranking.rankOf(30L)).contains(1);
        assertThat(ranking.rankOf(20L)).contains(3);
    }

    @Test
    void 랭킹에_없는_유저는_순위가_없다() {
        assertThat(ranking.rankOf(99L)).isEmpty();
        assertThat(LeagueRanking.empty().rankOf(1L)).isEmpty();
    }

    @Test
    void 순위로_해당_참여자를_찾는다() {
        assertThat(ranking.participantAt(2).userId()).isEqualTo(10L);
    }

    @Test
    void 전체_유저의_순위를_한꺼번에_반환한다() {
        assertThat(ranking.rankByUserId())
                .containsEntry(30L, 1)
                .containsEntry(10L, 2)
                .containsEntry(20L, 3)
                .hasSize(3);
    }

    private static LeagueParticipant participant(Long userId, long weeklySnackCount) {
        return new LeagueParticipant(userId, "유저" + userId, null, weeklySnackCount, 0, 0);
    }
}
