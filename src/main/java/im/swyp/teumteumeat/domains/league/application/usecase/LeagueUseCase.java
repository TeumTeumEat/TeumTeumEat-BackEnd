package im.swyp.teumteumeat.domains.league.application.usecase;

import im.swyp.teumteumeat.domains.league.application.component.LeagueRankingProvider;
import im.swyp.teumteumeat.domains.league.application.dto.response.LeagueMyRankResponse;
import im.swyp.teumteumeat.domains.league.application.dto.response.LeagueRankerResponse;
import im.swyp.teumteumeat.domains.league.application.dto.response.LeagueResponse;
import im.swyp.teumteumeat.domains.league.application.dto.response.LeagueResultResponse;
import im.swyp.teumteumeat.domains.league.application.mapper.LeagueMapper;
import im.swyp.teumteumeat.domains.league.domain.vo.LeagueParticipant;
import im.swyp.teumteumeat.domains.league.domain.vo.LeagueWeek;
import im.swyp.teumteumeat.domains.user.domain.service.UserService;
import im.swyp.teumteumeat.domains.user.persistence.entity.UserEntity;
import im.swyp.teumteumeat.global.annotation.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@UseCase
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeagueUseCase {

    private static final int RANKER_LIMIT = 10;

    private final LeagueRankingProvider leagueRankingProvider;
    private final UserService userService;

    public LeagueResponse getLeague(Long userId) {
        LeagueWeek week = LeagueWeek.of(LocalDateTime.now());
        List<LeagueParticipant> ranking = leagueRankingProvider.getRanking(week).participants();

        List<LeagueRankerResponse> rankers = new ArrayList<>();
        for (int i = 0; i < Math.min(RANKER_LIMIT, ranking.size()); i++) {
            rankers.add(LeagueMapper.toRankerResponse(ranking.get(i), i + 1, userId));
        }

        return LeagueMapper.toLeagueResponse(week, rankers, getMyRank(userId, ranking));
    }

    /**
     * 지난주 리그 최종 결과 중 내 순위 (리그 종료 후 결과 모달용)
     * 끝난 주차는 스낵 기록과 스트릭 기준일이 고정되어 있어 다시 계산해도 같은 결과가 나온다.
     */
    public LeagueResultResponse getLatestResult(Long userId) {
        LeagueWeek lastWeek = LeagueWeek.of(LocalDateTime.now()).previous();
        List<LeagueParticipant> ranking = leagueRankingProvider.getRanking(lastWeek).participants();

        for (int i = 0; i < ranking.size(); i++) {
            if (ranking.get(i).userId().equals(userId)) {
                return LeagueMapper.toResultResponse(lastWeek, ranking.get(i), i + 1);
            }
        }
        return LeagueMapper.toUnrankedResultResponse(lastWeek);
    }

    private LeagueMyRankResponse getMyRank(Long userId, List<LeagueParticipant> ranking) {
        for (int i = 0; i < ranking.size(); i++) {
            if (ranking.get(i).userId().equals(userId)) {
                return LeagueMapper.toMyRankResponse(ranking.get(i), i + 1);
            }
        }

        UserEntity user = userService.getUserById(userId);
        return LeagueMapper.toUnrankedMyRankResponse(user.getName());
    }
}
