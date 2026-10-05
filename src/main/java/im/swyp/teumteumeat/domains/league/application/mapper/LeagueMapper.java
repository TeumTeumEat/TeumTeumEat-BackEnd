package im.swyp.teumteumeat.domains.league.application.mapper;

import im.swyp.teumteumeat.domains.league.application.dto.response.LeagueMyRankResponse;
import im.swyp.teumteumeat.domains.league.application.dto.response.LeagueRankerResponse;
import im.swyp.teumteumeat.domains.league.application.dto.response.LeagueResponse;
import im.swyp.teumteumeat.domains.league.domain.util.NameMasker;
import im.swyp.teumteumeat.domains.league.domain.vo.LeagueParticipant;
import im.swyp.teumteumeat.domains.league.domain.vo.LeagueWeek;

import java.util.List;

public class LeagueMapper {

    public static LeagueRankerResponse toRankerResponse(LeagueParticipant participant, int rank, Long myUserId) {
        return LeagueRankerResponse.builder()
                .rank(rank)
                .name(NameMasker.mask(participant.name()))
                .weeklySnackCount(participant.weeklySnackCount())
                .isMe(participant.userId().equals(myUserId))
                .build();
    }

    public static LeagueMyRankResponse toMyRankResponse(LeagueParticipant me, int rank) {
        return LeagueMyRankResponse.builder()
                .rank(rank)
                .name(NameMasker.mask(me.name()))
                .weeklySnackCount(me.weeklySnackCount())
                .todaySnackCount(me.todaySnackCount())
                .build();
    }

    // 이번 주 스낵이 0개인 유저는 랭킹에서 제외되므로 순위 없이 0 스낵으로 표시
    public static LeagueMyRankResponse toUnrankedMyRankResponse(String name) {
        return LeagueMyRankResponse.builder()
                .rank(null)
                .name(NameMasker.mask(name))
                .weeklySnackCount(0)
                .todaySnackCount(0)
                .build();
    }

    public static LeagueResponse toLeagueResponse(
            LeagueWeek week,
            List<LeagueRankerResponse> rankers,
            LeagueMyRankResponse me) {
        return LeagueResponse.builder()
                .weekStartDate(week.weekStartDate())
                .resetAt(week.nextWeekStart())
                .remainingSeconds(week.remainingSeconds())
                .rankers(rankers)
                .me(me)
                .build();
    }
}
