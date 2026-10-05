package im.swyp.teumteumeat.domains.league.application.usecase;

import im.swyp.teumteumeat.domains.league.application.dto.response.LeagueMyRankResponse;
import im.swyp.teumteumeat.domains.league.application.dto.response.LeagueRankerResponse;
import im.swyp.teumteumeat.domains.league.application.dto.response.LeagueResponse;
import im.swyp.teumteumeat.domains.league.application.mapper.LeagueMapper;
import im.swyp.teumteumeat.domains.league.application.mapper.UserSnackCountMapping;
import im.swyp.teumteumeat.domains.league.domain.service.SnackHistoryService;
import im.swyp.teumteumeat.domains.league.domain.vo.LeagueParticipant;
import im.swyp.teumteumeat.domains.league.domain.vo.LeagueWeek;
import im.swyp.teumteumeat.domains.user.domain.service.UserService;
import im.swyp.teumteumeat.domains.user.persistence.entity.UserEntity;
import im.swyp.teumteumeat.domains.userQuiz.domain.service.UserQuizService;
import im.swyp.teumteumeat.global.annotation.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@UseCase
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeagueUseCase {

    private static final int RANKER_LIMIT = 10;

    private final SnackHistoryService snackHistoryService;
    private final UserService userService;
    private final UserQuizService userQuizService;

    public LeagueResponse getLeague(Long userId) {
        LeagueWeek week = LeagueWeek.of(LocalDateTime.now());
        List<LeagueParticipant> ranking = getRanking(week);

        List<LeagueRankerResponse> rankers = new ArrayList<>();
        for (int i = 0; i < Math.min(RANKER_LIMIT, ranking.size()); i++) {
            rankers.add(LeagueMapper.toRankerResponse(ranking.get(i), i + 1, userId));
        }

        return LeagueMapper.toLeagueResponse(week, rankers, getMyRank(userId, ranking));
    }

    /**
     * 이번 주 스낵을 1개 이상 모은 유저를 순위 순으로 정렬해 반환 (스낵 0개 유저는 제외)
     */
    private List<LeagueParticipant> getRanking(LeagueWeek week) {
        List<UserSnackCountMapping> snackCounts = snackHistoryService.getSnackCountsByUser(week);
        if (snackCounts.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> userIds = snackCounts.stream().map(UserSnackCountMapping::getUserId).toList();
        Map<Long, UserEntity> users = userService.getUsersByIds(userIds).stream()
                .collect(Collectors.toMap(UserEntity::getId, Function.identity()));
        Map<Long, Integer> streaks = userQuizService.calculateStreaksForUsers(userIds, week.streakReferenceDate());

        return snackCounts.stream()
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
