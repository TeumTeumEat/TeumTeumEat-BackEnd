package im.swyp.teumteumeat.domains.league.application.usecase;

import im.swyp.teumteumeat.domains.league.application.component.LeagueRankingProvider;
import im.swyp.teumteumeat.domains.league.application.dto.response.LeagueMyRankResponse;
import im.swyp.teumteumeat.domains.league.application.dto.response.LeagueRankerResponse;
import im.swyp.teumteumeat.domains.league.application.dto.response.LeagueResponse;
import im.swyp.teumteumeat.domains.league.application.dto.response.LeagueResultResponse;
import im.swyp.teumteumeat.domains.league.application.mapper.UserSnackCountMapping;
import im.swyp.teumteumeat.domains.league.domain.service.SnackHistoryService;
import im.swyp.teumteumeat.domains.league.domain.vo.LeagueWeek;
import im.swyp.teumteumeat.domains.user.domain.service.UserService;
import im.swyp.teumteumeat.domains.user.persistence.entity.UserEntity;
import im.swyp.teumteumeat.domains.userQuiz.domain.service.UserQuizService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// 리그 랭킹 정렬/상위 10명 제한/내 순위 계산 로직을 확인한다.
// 스낵 집계 쿼리는 목킹하고, 집계 결과를 받아 순위를 만드는 로직 자체만 검증한다. (캐시 미적용)
class LeagueUseCaseTest {

    private static final LocalDateTime BASE_JOINED_AT = LocalDateTime.of(2026, 1, 1, 0, 0);

    private SnackHistoryService snackHistoryService;
    private UserService userService;
    private UserQuizService userQuizService;
    private LeagueUseCase leagueUseCase;

    private final List<UserSnackCountMapping> snackCounts = new ArrayList<>();
    private final List<UserEntity> users = new ArrayList<>();
    private final Map<Long, Integer> streaks = new HashMap<>();

    @BeforeEach
    void setUp() {
        snackHistoryService = mock(SnackHistoryService.class);
        userService = mock(UserService.class);
        userQuizService = mock(UserQuizService.class);
        leagueUseCase = new LeagueUseCase(
                new LeagueRankingProvider(snackHistoryService, userService, userQuizService), userService);

        when(snackHistoryService.getSnackCountsByUser(any())).thenReturn(snackCounts);
        when(userService.getUsersByIds(anyList())).thenReturn(users);
        when(userQuizService.calculateStreaksForUsers(anyList(), any())).thenReturn(streaks);
    }

    @Test
    void 스낵_수_내림차순으로_정렬한다() {
        participant(1L, "가나다", 3, 0, 0);
        participant(2L, "라마바", 10, 0, 0);
        participant(3L, "사아자", 5, 0, 0);

        LeagueResponse response = leagueUseCase.getLeague(1L);

        assertThat(response.rankers()).extracting(LeagueRankerResponse::weeklySnackCount)
                .containsExactly(10L, 5L, 3L);
        assertThat(response.rankers()).extracting(LeagueRankerResponse::rank)
                .containsExactly(1, 2, 3);
    }

    @Test
    void 스낵_수가_같으면_스트릭이_높은_유저가_앞선다() {
        participant(1L, "가나다", 5, 2, 0);
        participant(2L, "라마바", 5, 7, 0);

        LeagueResponse response = leagueUseCase.getLeague(1L);

        assertThat(response.rankers()).extracting(LeagueRankerResponse::name)
                .containsExactly("라*바", "가*다");
    }

    @Test
    void 스낵_수와_스트릭이_같으면_가입일이_빠른_유저가_앞선다() {
        participant(1L, "가나다", 5, 3, 10);
        participant(2L, "라마바", 5, 3, 1);

        LeagueResponse response = leagueUseCase.getLeague(1L);

        assertThat(response.rankers()).extracting(LeagueRankerResponse::name)
                .containsExactly("라*바", "가*다");
    }

    @Test
    void 상위_10명만_반환하고_10위_밖인_내_순위는_me에_담는다() {
        for (long id = 1; id <= 12; id++) {
            participant(id, "유저" + id, 100 - id, 0, 0);
        }

        LeagueResponse response = leagueUseCase.getLeague(12L);

        assertThat(response.rankers()).hasSize(10);
        assertThat(response.rankers()).noneMatch(LeagueRankerResponse::isMe);
        assertThat(response.me().rank()).isEqualTo(12);
        assertThat(response.me().weeklySnackCount()).isEqualTo(88);
    }

    @Test
    void 순위권_안이면_랭커_목록에_본인_표시가_된다() {
        participant(1L, "가나다", 3, 0, 0);
        participant(2L, "라마바", 10, 0, 0);

        LeagueResponse response = leagueUseCase.getLeague(1L);

        assertThat(response.rankers().get(1).isMe()).isTrue();
        assertThat(response.me().rank()).isEqualTo(2);
        assertThat(response.me().todaySnackCount()).isEqualTo(1);
    }

    @Test
    void 이번_주_스낵이_없으면_순위_없이_0_스낵으로_표시한다() {
        participant(1L, "가나다", 3, 0, 0);
        UserEntity me = user(99L, "김지민", 0);
        when(userService.getUserById(99L)).thenReturn(me);

        LeagueResponse response = leagueUseCase.getLeague(99L);

        assertThat(response.rankers()).hasSize(1);
        assertThat(response.me().rank()).isNull();
        assertThat(response.me().name()).isEqualTo("김*민");
        assertThat(response.me().weeklySnackCount()).isZero();
        assertThat(response.me().todaySnackCount()).isZero();
    }

    @Test
    void 참여자가_없으면_빈_랭킹을_반환하고_스트릭을_조회하지_않는다() {
        UserEntity me = user(1L, "김지민", 0);
        when(userService.getUserById(1L)).thenReturn(me);

        LeagueResponse response = leagueUseCase.getLeague(1L);

        assertThat(response.rankers()).isEmpty();
        assertThat(response.me().rank()).isNull();
        verify(userQuizService, never()).calculateStreaksForUsers(anyList(), any());
    }

    @Test
    void 집계_이후_탈퇴해_조회되지_않는_유저는_랭킹에서_제외한다() {
        participant(1L, "가나다", 3, 0, 0);
        snackCounts.add(snackCount(2L, 10, 1));

        LeagueResponse response = leagueUseCase.getLeague(1L);

        assertThat(response.rankers()).hasSize(1);
        assertThat(response.me().rank()).isEqualTo(1);
    }

    @Test
    void 내_순위만_조회하면_리그_화면의_me와_같은_값을_반환한다() {
        participant(1L, "가나다", 3, 0, 0);
        participant(2L, "라마바", 10, 0, 0);

        LeagueMyRankResponse myRank = leagueUseCase.getMyRank(1L);

        assertThat(myRank).isEqualTo(leagueUseCase.getLeague(1L).me());
        assertThat(myRank.rank()).isEqualTo(2);
        assertThat(myRank.weeklySnackCount()).isEqualTo(3);
    }

    @Test
    void 이번_주_스낵이_없으면_내_순위는_null이다() {
        participant(1L, "가나다", 3, 0, 0);
        UserEntity me = user(99L, "김지민", 0);
        when(userService.getUserById(99L)).thenReturn(me);

        LeagueMyRankResponse myRank = leagueUseCase.getMyRank(99L);

        assertThat(myRank.rank()).isNull();
        assertThat(myRank.name()).isEqualTo("김*민");
        assertThat(myRank.weeklySnackCount()).isZero();
    }

    @Test
    void 지난주_결과는_직전_주차로_집계하고_스트릭은_그_주_일요일_기준으로_계산한다() {
        participant(1L, "가나다", 3, 0, 0);
        LeagueWeek expectedWeek = LeagueWeek.of(LocalDateTime.now()).previous();

        leagueUseCase.getLatestResult(1L);

        ArgumentCaptor<LeagueWeek> weekCaptor = ArgumentCaptor.forClass(LeagueWeek.class);
        verify(snackHistoryService).getSnackCountsByUser(weekCaptor.capture());
        assertThat(weekCaptor.getValue().weekStart()).isEqualTo(expectedWeek.weekStart());
        assertThat(weekCaptor.getValue().nextWeekStart()).isEqualTo(expectedWeek.weekStart().plusWeeks(1));

        LocalDate lastSunday = expectedWeek.nextWeekStart().toLocalDate().minusDays(1);
        verify(userQuizService).calculateStreaksForUsers(anyList(), eq(lastSunday));
    }

    @Test
    void 지난주_랭킹에_있으면_최종_순위와_스낵_수를_반환한다() {
        participant(1L, "가나다", 3, 0, 0);
        participant(2L, "라마바", 10, 0, 0);

        LeagueResultResponse response = leagueUseCase.getLatestResult(1L);

        assertThat(response.rank()).isEqualTo(2);
        assertThat(response.weeklySnackCount()).isEqualTo(3);
        assertThat(response.weekStartDate())
                .isEqualTo(LeagueWeek.of(LocalDateTime.now()).previous().weekStartDate());
    }

    @Test
    void 지난주_스낵이_없으면_순위_없이_반환한다() {
        participant(1L, "가나다", 3, 0, 0);

        LeagueResultResponse response = leagueUseCase.getLatestResult(99L);

        assertThat(response.rank()).isNull();
        assertThat(response.weeklySnackCount()).isZero();
    }

    private void participant(Long userId, String name, long weeklySnackCount, int streak, int joinedDaysAfterBase) {
        snackCounts.add(snackCount(userId, weeklySnackCount, 1));
        users.add(user(userId, name, joinedDaysAfterBase));
        streaks.put(userId, streak);
    }

    private UserEntity user(Long userId, String name, int joinedDaysAfterBase) {
        UserEntity user = mock(UserEntity.class);
        when(user.getId()).thenReturn(userId);
        when(user.getName()).thenReturn(name);
        when(user.getCreatedDate()).thenReturn(BASE_JOINED_AT.plusDays(joinedDaysAfterBase));
        return user;
    }

    private UserSnackCountMapping snackCount(Long userId, long weekly, long today) {
        return new UserSnackCountMapping() {
            @Override
            public Long getUserId() {
                return userId;
            }

            @Override
            public Long getWeeklySnackCount() {
                return weekly;
            }

            @Override
            public Long getTodaySnackCount() {
                return today;
            }
        };
    }
}
