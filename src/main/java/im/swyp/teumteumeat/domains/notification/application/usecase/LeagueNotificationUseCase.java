package im.swyp.teumteumeat.domains.notification.application.usecase;

import im.swyp.teumteumeat.domains.league.application.component.LeagueRankingProvider;
import im.swyp.teumteumeat.domains.league.domain.vo.LeagueWeek;
import im.swyp.teumteumeat.domains.notification.application.mapper.PushMessageMapper;
import im.swyp.teumteumeat.domains.notification.domain.constant.LeagueNotificationProperties;
import im.swyp.teumteumeat.domains.notification.domain.constant.NotificationType;
import im.swyp.teumteumeat.domains.user.domain.service.UserService;
import im.swyp.teumteumeat.domains.user.persistence.entity.UserEntity;
import im.swyp.teumteumeat.global.annotation.UseCase;
import im.swyp.teumteumeat.infra.fcm.domain.FcmService;
import im.swyp.teumteumeat.infra.fcm.dto.PushMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class LeagueNotificationUseCase {

    private final LeagueRankingProvider leagueRankingProvider;
    private final UserService userService;
    private final FcmService fcmService;
    private final LeagueNotificationProperties leagueNotificationProperties;

    /**
     * 리그 마감 전 참여 독려 알림 (순위가 있으면 현재 순위 포함, 없으면 기본 문구)
     */
    public void sendDeadlineNotifications(LocalDateTime now) {
        Map<Long, Integer> ranks = leagueRankingProvider.getRanking(LeagueWeek.of(now)).rankByUserId();

        List<PushMessage> pushMessages = new ArrayList<>();
        for (UserEntity user : userService.getAllWithTokensByPushEnabled()) {
            String body = resolveDeadlineBody(ranks.get(user.getId()));
            pushMessages.add(PushMessageMapper.toPushMessage(
                    user, leagueNotificationProperties.getTitle(), body, NotificationType.LEAGUE_DEADLINE)
            );
        }

        log.debug("League deadline notifications: users with rank={}, push users={}", ranks.size(), pushMessages.size());
        fcmService.send(pushMessages);
    }

    /**
     * 지난주 리그 결과 확인 알림 (지난주 순위에 든 유저에게만 발송)
     */
    public void sendResultNotifications(LocalDateTime now) {
        Map<Long, Integer> ranks = leagueRankingProvider.getRanking(LeagueWeek.of(now).previous()).rankByUserId();
        String body = leagueNotificationProperties.getResultMessage();

        List<PushMessage> pushMessages = new ArrayList<>();
        for (UserEntity user : userService.getAllWithTokensByPushEnabled()) {
            if (!ranks.containsKey(user.getId())) {
                continue;
            }
            pushMessages.add(PushMessageMapper.toPushMessage(
                    user, leagueNotificationProperties.getTitle(), body, NotificationType.LEAGUE_RESULT)
            );
        }

        log.debug("League result notifications: users with rank={}, push users={}", ranks.size(), pushMessages.size());
        fcmService.send(pushMessages);
    }

    String resolveDeadlineBody(Integer rank) {
        if (rank == null) {
            return leagueNotificationProperties.getDeadlineUnrankedMessage();
        }
        return leagueNotificationProperties.getDeadlineRankedMessage()
                .replace("{rank}", String.valueOf(rank));
    }
}
