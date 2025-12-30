package im.swyp.teumteumeat.domains.userQuiz.domain.service;

import java.util.List;
import im.swyp.teumteumeat.domains.userQuiz.persistence.entity.UserQuiz;
import im.swyp.teumteumeat.domains.userQuiz.persistence.repository.UserQuizRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    public List<UserQuiz> getAllQuizzes(Long userId) {
        return userQuizRepository.findAllByUserIdOrderByCreatedDateDesc(userId);
    }

    public List<LocalDate> getDistinctStudyDays(Long userId) {
        return userQuizRepository.findDistinctDaysByUserId(userId).stream()
                .map(java.sql.Date::toLocalDate)
                .toList();
    }

    public int calculateCurrentStreak(Long userId) {
        List<LocalDate> days = getDistinctStudyDays(userId);
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

    public List<Long> getConsumedDocumentIds(Long userId) {
        return userQuizRepository.findConsumedDocumentIdsByUserId(userId);
    }

    @Transactional(readOnly = true)
    public boolean hasSolvedQuizToday(Long userId, Long categoryId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        return userQuizRepository.existsByUserIdAndQuiz_CategoryDocument_Category_IdAndCreatedDateBetween(
                userId, categoryId, startOfDay, endOfDay);
    }

    @Transactional(readOnly = true)
    public boolean hasSolvedQuizTodayByGoal(Long userId, Long goalId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        return userQuizRepository.existsByUserIdAndQuiz_Document_Goal_IdAndCreatedDateBetween(
                userId, goalId, startOfDay, endOfDay);
    }
}
