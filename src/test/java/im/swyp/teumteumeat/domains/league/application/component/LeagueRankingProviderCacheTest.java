package im.swyp.teumteumeat.domains.league.application.component;

import im.swyp.teumteumeat.domains.league.domain.service.SnackHistoryService;
import im.swyp.teumteumeat.domains.league.domain.vo.LeagueWeek;
import im.swyp.teumteumeat.domains.user.domain.service.UserService;
import im.swyp.teumteumeat.domains.userQuiz.domain.service.UserQuizService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// @Cacheable 키와 동작을 확인한다.
// Redis 대신 같은 이름의 인메모리 CacheManager를 등록해 캐시 프록시 동작만 검증한다.
@SpringJUnitConfig(LeagueRankingProviderCacheTest.Config.class)
class LeagueRankingProviderCacheTest {

    private static final LeagueWeek THIS_WEEK = LeagueWeek.of(LocalDateTime.of(2026, 10, 8, 14, 0));

    @Configuration
    @EnableCaching
    static class Config {

        @Bean
        CacheManager redisCacheManager() {
            return new ConcurrentMapCacheManager(LeagueRankingProvider.CACHE_NAME);
        }

        @Bean
        SnackHistoryService snackHistoryService() {
            return mock(SnackHistoryService.class);
        }

        @Bean
        LeagueRankingProvider leagueRankingProvider(SnackHistoryService snackHistoryService) {
            return new LeagueRankingProvider(snackHistoryService, mock(UserService.class), mock(UserQuizService.class));
        }
    }

    @Autowired
    private LeagueRankingProvider leagueRankingProvider;

    @Autowired
    private SnackHistoryService snackHistoryService;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        cacheManager.getCache(LeagueRankingProvider.CACHE_NAME).clear();
        clearInvocations(snackHistoryService);
        when(snackHistoryService.getSnackCountsByUser(any())).thenReturn(List.of());
    }

    @Test
    void 같은_주차는_한_번만_계산하고_이후에는_캐시를_사용한다() {
        leagueRankingProvider.getRanking(THIS_WEEK);
        leagueRankingProvider.getRanking(THIS_WEEK);

        verify(snackHistoryService, times(1)).getSnackCountsByUser(any());
    }

    @Test
    void 날짜가_바뀌면_같은_주라도_다시_계산한다() {
        leagueRankingProvider.getRanking(THIS_WEEK);

        leagueRankingProvider.getRanking(LeagueWeek.of(LocalDateTime.of(2026, 10, 9, 0, 1)));

        verify(snackHistoryService, times(2)).getSnackCountsByUser(any());
    }
}
