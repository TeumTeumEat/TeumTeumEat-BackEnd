package im.swyp.teumteumeat.global.config.cache;

import im.swyp.teumteumeat.domains.league.domain.vo.LeagueParticipant;
import im.swyp.teumteumeat.domains.league.domain.vo.LeagueRanking;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// 리그 랭킹 캐시 값이 Redis 직렬화/역직렬화 후에도 같은 타입·값으로 복원되는지 확인한다.
class CacheConfigTest {

    @Test
    void 리그_랭킹은_직렬화_후_같은_record로_복원된다() {
        Jackson2JsonRedisSerializer<LeagueRanking> serializer = CacheConfig.jsonSerializer(LeagueRanking.class);
        LeagueRanking ranking = new LeagueRanking(List.of(
                new LeagueParticipant(1L, "김지민", LocalDateTime.of(2026, 1, 1, 12, 30, 15), 10, 2, 5),
                new LeagueParticipant(2L, null, null, 3, 0, 0)));

        LeagueRanking restored = serializer.deserialize(serializer.serialize(ranking));

        assertThat(restored).isEqualTo(ranking);
        assertThat(restored.participants().get(0)).isInstanceOf(LeagueParticipant.class);
    }

    @Test
    void 빈_랭킹도_복원된다() {
        Jackson2JsonRedisSerializer<LeagueRanking> serializer = CacheConfig.jsonSerializer(LeagueRanking.class);

        LeagueRanking restored = serializer.deserialize(serializer.serialize(LeagueRanking.empty()));

        assertThat(restored.participants()).isEmpty();
    }
}
