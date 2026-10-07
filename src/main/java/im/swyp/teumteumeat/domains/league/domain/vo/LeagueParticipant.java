package im.swyp.teumteumeat.domains.league.domain.vo;

import java.time.LocalDateTime;
import java.util.Comparator;

/**
 * 이번 주 스낵을 1개 이상 모은 리그 참여자
 */
public record LeagueParticipant(
        Long userId,
        String name,
        LocalDateTime joinedAt,
        long weeklySnackCount,
        long todaySnackCount,
        int streak
) {

    /**
     * 순위 정렬 기준: 주간 스낵 수 내림차순 → 스트릭 내림차순 → 가입일 오름차순 → 유저 ID 오름차순
     */
    public static final Comparator<LeagueParticipant> RANKING_ORDER = Comparator
            .comparingLong(LeagueParticipant::weeklySnackCount).reversed()
            .thenComparing(Comparator.comparingInt(LeagueParticipant::streak).reversed())
            .thenComparing(LeagueParticipant::joinedAt, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(LeagueParticipant::userId);
}
