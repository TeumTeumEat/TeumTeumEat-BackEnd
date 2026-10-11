package im.swyp.teumteumeat.domains.league.application.usecase;

import im.swyp.teumteumeat.domains.league.application.component.LeagueRankingProvider;
import im.swyp.teumteumeat.domains.league.application.component.LeagueTimeResolver;
import im.swyp.teumteumeat.domains.league.domain.vo.LeagueRanking;
import im.swyp.teumteumeat.domains.user.domain.service.UserService;
import im.swyp.teumteumeat.domains.user.persistence.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// 시각 오프셋 요청은 랭킹 캐시를 거치지 않고, 일반 요청은 캐시를 사용하는지 확인한다.
class LeagueUseCaseCacheRoutingTest {

    private static final long OFFSET = 60L;

    private LeagueRankingProvider leagueRankingProvider;
    private UserService userService;

    @BeforeEach
    void setUp() {
        leagueRankingProvider = mock(LeagueRankingProvider.class);
        userService = mock(UserService.class);
        when(leagueRankingProvider.getRanking(any())).thenReturn(LeagueRanking.empty());
        when(leagueRankingProvider.calculateRanking(any())).thenReturn(LeagueRanking.empty());
        when(userService.getUserById(anyLong())).thenReturn(mock(UserEntity.class));
    }

    @Test
    void 헤더가_없으면_캐시를_사용한다() {
        LeagueUseCase leagueUseCase = new LeagueUseCase(leagueRankingProvider, new LeagueTimeResolver(true), userService);

        leagueUseCase.getLeague(1L, null);
        leagueUseCase.getMyRank(1L, null);
        leagueUseCase.getLatestResult(1L, null);

        verify(leagueRankingProvider, never()).calculateRanking(any());
    }

    @Test
    void 오프셋_요청은_캐시_없이_계산한다() {
        LeagueUseCase leagueUseCase = new LeagueUseCase(leagueRankingProvider, new LeagueTimeResolver(true), userService);

        leagueUseCase.getLeague(1L, OFFSET);
        leagueUseCase.getMyRank(1L, OFFSET);
        leagueUseCase.getLatestResult(1L, OFFSET);

        verify(leagueRankingProvider, never()).getRanking(any());
    }

    @Test
    void 비활성화_환경에서는_헤더가_있어도_캐시를_사용한다() {
        LeagueUseCase leagueUseCase = new LeagueUseCase(leagueRankingProvider, new LeagueTimeResolver(false), userService);

        leagueUseCase.getLeague(1L, OFFSET);

        verify(leagueRankingProvider).getRanking(any());
        verify(leagueRankingProvider, never()).calculateRanking(any());
    }
}
