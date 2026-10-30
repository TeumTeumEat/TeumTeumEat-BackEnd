package im.swyp.teumteumeat.domains.userQuiz.domain.service;

import im.swyp.teumteumeat.domains.notification.application.mapper.UserStudyDateMapping;
import im.swyp.teumteumeat.domains.userQuiz.persistence.repository.UserQuizRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// 기준일을 지정한 스트릭 계산이 기준일 이후 학습 기록을 무시하는지 확인한다.
class UserQuizServiceStreakTest {

    private static final Long USER_ID = 1L;

    private UserQuizRepository userQuizRepository;
    private UserQuizService userQuizService;

    @BeforeEach
    void setUp() {
        userQuizRepository = mock(UserQuizRepository.class);
        userQuizService = new UserQuizService(userQuizRepository);
    }

    @Test
    void 기준일까지_연속으로_학습한_일수를_센다() {
        studyDates(LocalDate.of(2026, 10, 4), LocalDate.of(2026, 10, 3), LocalDate.of(2026, 10, 2),
                LocalDate.of(2026, 9, 30));

        int streak = userQuizService.calculateStreaksForUsers(List.of(USER_ID), LocalDate.of(2026, 10, 4))
                .get(USER_ID);

        assertThat(streak).isEqualTo(3);
    }

    @Test
    void 기준일_이후의_학습_기록은_무시한다() {
        studyDates(LocalDate.of(2026, 10, 6), LocalDate.of(2026, 10, 5),
                LocalDate.of(2026, 10, 4), LocalDate.of(2026, 10, 3));

        int streak = userQuizService.calculateStreaksForUsers(List.of(USER_ID), LocalDate.of(2026, 10, 4))
                .get(USER_ID);

        assertThat(streak).isEqualTo(2);
    }

    @Test
    void 기준일_전날까지_학습했다면_스트릭이_유지된다() {
        studyDates(LocalDate.of(2026, 10, 3), LocalDate.of(2026, 10, 2));

        int streak = userQuizService.calculateStreaksForUsers(List.of(USER_ID), LocalDate.of(2026, 10, 4))
                .get(USER_ID);

        assertThat(streak).isEqualTo(2);
    }

    @Test
    void 기준일_이전에_이틀_이상_쉬었다면_스트릭은_0이다() {
        studyDates(LocalDate.of(2026, 10, 6), LocalDate.of(2026, 10, 1));

        int streak = userQuizService.calculateStreaksForUsers(List.of(USER_ID), LocalDate.of(2026, 10, 4))
                .get(USER_ID);

        assertThat(streak).isZero();
    }

    // 최신순으로 전달
    private void studyDates(LocalDate... dates) {
        List<UserStudyDateMapping> mappings = java.util.Arrays.stream(dates)
                .map(date -> (UserStudyDateMapping) new UserStudyDateMapping() {
                    @Override
                    public Long getUserId() {
                        return USER_ID;
                    }

                    @Override
                    public LocalDate getStudyDate() {
                        return date;
                    }
                })
                .toList();
        when(userQuizRepository.findUserStudyDates(anyList())).thenReturn(mappings);
    }
}
