package im.swyp.teumteumeat.domains.userQuiz.domain.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import im.swyp.teumteumeat.domains.notification.application.mapper.UserStudyDateMapping;
import im.swyp.teumteumeat.domains.quiz.persistence.entity.Quiz;
import im.swyp.teumteumeat.domains.user.persistence.entity.UserEntity;
import im.swyp.teumteumeat.domains.userQuiz.persistence.entity.UserQuiz;
import im.swyp.teumteumeat.domains.userQuiz.persistence.repository.UserQuizRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQuizService {

    private final UserQuizRepository userQuizRepository;

    @Transactional
    public void saveUserQuiz(UserQuiz userQuiz) {
        userQuizRepository.save(userQuiz);
    }

    public List<UserQuiz> getQuizzesByDateRange(Long userId, LocalDateTime start, LocalDateTime end) {
        return userQuizRepository.findAllByUserIdAndCreatedDateBetween(userId, start, end);
    }

    public java.util.Optional<UserQuiz> getQuizByDate(
            UserEntity user,
            Quiz quiz,
            LocalDateTime start, LocalDateTime end) {
        return userQuizRepository.findByUserAndQuizAndCreatedDateBetween(user, quiz, start, end);
    }

    public List<UserQuiz> getAllQuizzes(Long userId) {
        return userQuizRepository.findAllByUserIdOrderByCreatedDateDesc(userId);
    }

    public List<Long> getConsumedDocumentIds(Long userId) {
        return userQuizRepository.findConsumedDocumentIdsByUserId(userId);
    }

    public boolean hasSolvedQuizToday(Long userId, Long categoryId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        return userQuizRepository.existsByUserIdAndQuiz_CategoryDocument_Category_IdAndCreatedDateBetween(
                userId, categoryId, startOfDay, endOfDay);
    }

    public boolean hasSolvedQuizTodayByGoal(Long userId, Long goalId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        return userQuizRepository.existsByUserIdAndQuiz_Document_Goal_IdAndCreatedDateBetween(
                userId, goalId, startOfDay, endOfDay);
    }

    public boolean hasSolvedAnyQuizToday(Long userId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        return !userQuizRepository.findAllByUserIdAndCreatedDateBetween(userId, startOfDay, endOfDay).isEmpty();
    }

    public List<UserEntity> getAllUsersByHasSolvedAnyQuizToday() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        return userQuizRepository.findDistinctUsersByQuizDate(startOfDay, endOfDay);
    }

    public int getTotalStudyDays(Long userId) {
        return userQuizRepository.countDistinctDaysByUserId(userId);
    }

    public boolean hasSolvedAnyQuizEver(Long userId) {
        return !userQuizRepository.findAllByUserIdOrderByCreatedDateDesc(userId).isEmpty();
    }

    @Transactional
    public void deleteAllByUserId(Long userId) {
        userQuizRepository.deleteAllByUserId(userId);
    }

    /**
     * 유저의 스트릭 일 수 반환
     * @param userId 유저 ID
     * @return 스트릭 수
     * @see #calculateStreaksForUsers
     */
    public int calculateStreakForUser(Long userId) {
        Map<Long, Integer> result = calculateStreaksForUsers(List.of(userId));
        return result.get(userId);
    }

    /**
     * 유저별로 스트릭 일 수를 계산하여 반환
     * @param userIds 조회 대상 유저 목록
     * @return 유저별 스트릭 일 수
     */
    public Map<Long, Integer> calculateStreaksForUsers(List<Long> userIds) {
        // 유저별로 학습 날짜 그룹화
        List<UserStudyDateMapping> userStudyDates = userQuizRepository.findUserStudyDates(userIds);
        Map<Long, List<LocalDate>> userDaysMap = userStudyDates.stream()
                .collect(Collectors.groupingBy(
                        UserStudyDateMapping::getUserId,
                        Collectors.mapping(UserStudyDateMapping::getStudyDate, Collectors.toList())
                ));

        // 유저별 스트릭 계산
        Map<Long, Integer> streakMap = new HashMap<>();
        for (Long userId : userIds) {
            List<LocalDate> days = userDaysMap.getOrDefault(userId, Collections.emptyList());
            streakMap.put(userId, calculateStreak(days));
        }
        return streakMap;
    }

    //* HELPER METHOD *//
    /**
     * 스트릭 일 수 계산
     * @param days 날짜 목록
     * @return 스트릭 일 수
     */
    private int calculateStreak(List<LocalDate> days) {
        if (days.isEmpty())
            return 0;

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        // 가장 최근 학습일이 오늘이나 어제가 아니면 스트릭 끊김
        LocalDate lastStudyDate = days.get(0);
        if (!lastStudyDate.isEqual(today) && !lastStudyDate.isEqual(yesterday)) {
            return 0;
        }

        int streak = 0;
        LocalDate checkDate = lastStudyDate;

        for (LocalDate date : days) {
            if (date.isEqual(checkDate)) {
                streak++;
                checkDate = checkDate.minusDays(1);
            } else {
                break;
            }
        }
        return streak;
    }
}
