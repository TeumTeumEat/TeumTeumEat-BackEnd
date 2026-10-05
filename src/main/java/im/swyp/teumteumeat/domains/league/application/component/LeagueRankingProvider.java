package im.swyp.teumteumeat.domains.league.application.component;

import im.swyp.teumteumeat.domains.league.application.mapper.UserSnackCountMapping;
import im.swyp.teumteumeat.domains.league.domain.service.SnackHistoryService;
import im.swyp.teumteumeat.domains.league.domain.vo.LeagueParticipant;
import im.swyp.teumteumeat.domains.league.domain.vo.LeagueRanking;
import im.swyp.teumteumeat.domains.league.domain.vo.LeagueWeek;
import im.swyp.teumteumeat.domains.user.domain.service.UserService;
import im.swyp.teumteumeat.domains.user.persistence.entity.UserEntity;
import im.swyp.teumteumeat.domains.userQuiz.domain.service.UserQuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 주차별 리그 랭킹 계산 및 캐시
 * - 랭킹은 모든 유저에게 동일하므로 주차 단위로 한 번만 계산해 Redis에 캐시한다.
 * - 스낵이 적립되면 커밋 후 해당 주차 캐시를 삭제하고, 다음 조회 시 다시 계산한다. (TTL은 안전장치)
 * - 같은 클래스 내부 호출은 캐시 프록시를 거치지 않으므로 UseCase와 분리한다.
 */
@Component
@RequiredArgsConstructor
public class LeagueRankingProvider {

    public static final String CACHE_NAME = "leagueRanking";

    private final SnackHistoryService snackHistoryService;
    private final UserService userService;
    private final UserQuizService userQuizService;

    /**
     * 해당 주차에 스낵을 1개 이상 모은 유저를 순위 순으로 정렬해 반환 (스낵 0개 유저는 제외)
     * 캐시가 비어 있을 때 동시에 여러 요청이 와도 한 요청만 계산하도록 sync 처리한다.
     */
    @Cacheable(cacheNames = CACHE_NAME, cacheManager = "redisCacheManager", key = "#week.cacheKey()", sync = true)
    public LeagueRanking getRanking(LeagueWeek week) {
        List<UserSnackCountMapping> snackCounts = snackHistoryService.getSnackCountsByUser(week);
        if (snackCounts.isEmpty()) {
            return LeagueRanking.empty();
        }

        List<Long> userIds = snackCounts.stream().map(UserSnackCountMapping::getUserId).toList();
        Map<Long, UserEntity> users = userService.getUsersByIds(userIds).stream()
                .collect(Collectors.toMap(UserEntity::getId, Function.identity()));
        Map<Long, Integer> streaks = userQuizService.calculateStreaksForUsers(userIds, week.streakReferenceDate());

        List<LeagueParticipant> participants = snackCounts.stream()
                .filter(count -> users.containsKey(count.getUserId()))
                .map(count -> {
                    UserEntity user = users.get(count.getUserId());
                    return new LeagueParticipant(
                            user.getId(),
                            user.getName(),
                            user.getCreatedDate(),
                            count.getWeeklySnackCount(),
                            count.getTodaySnackCount(),
                            streaks.getOrDefault(user.getId(), 0));
                })
                .sorted(LeagueParticipant.RANKING_ORDER)
                .toList();

        return new LeagueRanking(participants);
    }

    @CacheEvict(cacheNames = CACHE_NAME, cacheManager = "redisCacheManager", key = "#week.cacheKey()")
    public void evict(LeagueWeek week) {
    }
}
