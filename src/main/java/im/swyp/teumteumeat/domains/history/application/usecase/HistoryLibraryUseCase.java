package im.swyp.teumteumeat.domains.history.application.usecase;

import im.swyp.teumteumeat.domains.categoryDocument.persistence.entity.CategoryDocument;
import im.swyp.teumteumeat.domains.document.persistence.entity.Document;
import im.swyp.teumteumeat.domains.goal.domain.constant.GoalType;
import im.swyp.teumteumeat.domains.history.application.dto.response.*;
import im.swyp.teumteumeat.domains.quiz.application.dto.response.QuizListResponse;
import im.swyp.teumteumeat.domains.quiz.application.mapper.QuizMapper;
import im.swyp.teumteumeat.domains.quiz.persistence.entity.Quiz;
import im.swyp.teumteumeat.domains.userQuiz.application.dto.response.QuizSetResponse;
import im.swyp.teumteumeat.domains.userQuiz.persistence.entity.UserQuiz;
import im.swyp.teumteumeat.domains.userQuiz.persistence.repository.UserQuizRepository;
import im.swyp.teumteumeat.global.annotation.UseCase;
import im.swyp.teumteumeat.global.exception.BaseException;
import im.swyp.teumteumeat.global.common.CommonResponseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@UseCase
@RequiredArgsConstructor
public class HistoryLibraryUseCase {

    private final UserQuizRepository userQuizRepository;
    private final QuizMapper quizMapper;

    @Transactional(readOnly = true)
    public CalendarResponse getCalendar(Long userId) {
        // 이번 달 기준
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).with(LocalTime.MIN);
        LocalDateTime endOfMonth = now.withDayOfMonth(now.toLocalDate().lengthOfMonth()).with(LocalTime.MAX);

        List<UserQuiz> monthlyQuizzes = userQuizRepository.findAllByUserIdAndCreatedDateBetween(userId, startOfMonth,
                endOfMonth);

        // 스탬프 날짜 추출 (중복 제거)
        List<LocalDate> stampedDates = monthlyQuizzes.stream()
                .map(uq -> uq.getCreatedDate().toLocalDate())
                .distinct()
                .sorted()
                .toList();

        int currentStreak = calculateStreak(userId);

        return CalendarResponse.builder()
                .stampedDates(stampedDates)
                .totalStamps(stampedDates.size())
                .currentStreak(currentStreak)
                .build();
    }

    private int calculateStreak(Long userId) {
        List<java.sql.Date> rawDays = userQuizRepository.findDistinctDaysByUserId(userId);
        if (rawDays.isEmpty())
            return 0;

        List<LocalDate> days = rawDays.stream()
                .map(java.sql.Date::toLocalDate)
                .toList();

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

    @Transactional(readOnly = true)
    public List<DailyHistoryResponse> getDailyHistory(Long userId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        List<UserQuiz> quizzes = userQuizRepository.findAllByUserIdAndCreatedDateBetween(userId, startOfDay, endOfDay);

        // Document/CategoryDocument 단위로 그룹화
        // Set을 사용하여 중복 제거 (같은 문서를 여러 번 풀어도 한 번만 표시)
        Set<String> processedKeys = new HashSet<>();
        List<DailyHistoryResponse> result = new ArrayList<>();

        for (UserQuiz uq : quizzes) {
            Quiz quiz = uq.getQuiz();
            String key;
            DailyHistoryResponse item = null;

            if (quiz.getDocument() != null) {
                Document doc = quiz.getDocument();
                key = "DOC_" + doc.getId();
                if (!processedKeys.contains(key)) {
                    item = DailyHistoryResponse.builder()
                            .id(doc.getId())
                            .type(GoalType.DOCUMENT)
                            .title(doc.getFileName()) // PDF 파일명
                            .summarySnippet(getSnippet(doc.getSummary()))
                            .lastStudiedAt(uq.getCreatedDate())
                            .build();
                }
            } else if (quiz.getCategoryDocument() != null) {
                CategoryDocument doc = quiz.getCategoryDocument();
                key = "CAT_" + doc.getId();
                if (!processedKeys.contains(key)) {
                    item = DailyHistoryResponse.builder()
                            .id(doc.getId())
                            .type(GoalType.CATEGORY)
                            .title(doc.getCategory().getName()) // 카테고리명
                            .summarySnippet(getSnippet(doc.getContent()))
                            .lastStudiedAt(uq.getCreatedDate())
                            .build();
                }
            } else {
                continue;
            }

            if (item != null) {
                processedKeys.add(key);
                result.add(item);
            }
        }

        return result;
    }

    private String getSnippet(String fullText) {
        if (fullText == null)
            return "";
        return fullText.length() > 50 ? fullText.substring(0, 50) + "..." : fullText;
    }

    @Transactional(readOnly = true)
    public List<TopicHistoryResponse> getTopicHistory(Long userId) {
        // 전체 기록 조회 (유저 학습 시작일 ~ 현재)
        List<UserQuiz> allQuizzes = userQuizRepository.findAllByUserIdAndCreatedDateBetween(
                userId, LocalDateTime.of(2023, 1, 1, 0, 0), LocalDateTime.now());

        Map<String, List<DailyHistoryResponse>> grouped = new HashMap<>();
        Set<String> processedKeys = new HashSet<>();

        // 최신순 정렬
        allQuizzes.sort((a, b) -> b.getCreatedDate().compareTo(a.getCreatedDate()));

        for (UserQuiz uq : allQuizzes) {
            Quiz quiz = uq.getQuiz();
            String categoryName = "기타";
            DailyHistoryResponse item = null;
            String uniqueKey;

            if (quiz.getDocument() != null) {
                Document doc = quiz.getDocument();
                categoryName = "PDF 자료"; // 카테고리 고려하기..
                uniqueKey = "DOC_" + doc.getId();

                if (!processedKeys.contains(uniqueKey)) {
                    item = DailyHistoryResponse.builder()
                            .id(doc.getId())
                            .type(GoalType.DOCUMENT)
                            .title(doc.getFileName())
                            .summarySnippet(getSnippet(doc.getSummary()))
                            .lastStudiedAt(uq.getCreatedDate())
                            .build();
                }

            } else if (quiz.getCategoryDocument() != null) {
                CategoryDocument doc = quiz.getCategoryDocument();
                categoryName = doc.getCategory().getName();
                uniqueKey = "CAT_" + doc.getId();

                if (!processedKeys.contains(uniqueKey)) {
                    item = DailyHistoryResponse.builder()
                            .id(doc.getId())
                            .type(GoalType.CATEGORY)
                            .title(categoryName)
                            .summarySnippet(getSnippet(doc.getContent()))
                            .lastStudiedAt(uq.getCreatedDate())
                            .build();
                }
            } else {
                continue;
            }

            if (item != null) {
                processedKeys.add(uniqueKey);
                grouped.computeIfAbsent(categoryName, k -> new ArrayList<>()).add(item);
            }
        }

        return grouped.entrySet().stream()
                .map(entry -> TopicHistoryResponse.builder()
                        .categoryName(entry.getKey())
                        .histories(entry.getValue())
                        .build())
                .toList();
    }

    // date 포함 버전
    @Transactional(readOnly = true)
    public HistorySummaryResponse getHistorySummary(Long userId, GoalType type, Long id, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        List<UserQuiz> quizzes = userQuizRepository.findAllByUserIdAndCreatedDateBetween(userId, startOfDay, endOfDay);

        // 필터링 및 첫 번째 항목 찾기
        UserQuiz targetQuiz = quizzes.stream()
                .filter(uq -> {
                    if (type == GoalType.DOCUMENT)
                        return uq.getQuiz().getDocument() != null && uq.getQuiz().getDocument().getId().equals(id);
                    if (type == GoalType.CATEGORY)
                        return uq.getQuiz().getCategoryDocument() != null
                                && uq.getQuiz().getCategoryDocument().getId().equals(id);
                    return false;
                })
                .findFirst()
                .orElseThrow(() -> new BaseException(CommonResponseCode.NOT_FOUND));

        String title;
        String summary;
        Quiz quiz = targetQuiz.getQuiz();

        if (type == GoalType.DOCUMENT) {
            title = quiz.getDocument().getFileName();
            summary = quiz.getDocument().getSummary();
        } else {
            title = quiz.getCategoryDocument().getCategory().getName();
            summary = quiz.getCategoryDocument().getContent();
        }

        return HistorySummaryResponse.builder()
                .title(title)
                .summary(summary)
                .createdAt(startOfDay)
                .build();
    }

    @Transactional(readOnly = true)
    public HistoryQuizListResponse getHistoryQuizzes(Long userId, GoalType type, Long id, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        List<UserQuiz> quizzes = userQuizRepository.findAllByUserIdAndCreatedDateBetween(userId, startOfDay, endOfDay);

        List<QuizListResponse.QuizDto> quizDtos = quizzes
                .stream()
                .filter(uq -> {
                    if (type == GoalType.DOCUMENT)
                        return uq.getQuiz().getDocument() != null && uq.getQuiz().getDocument().getId().equals(id);
                    if (type == GoalType.CATEGORY)
                        return uq.getQuiz().getCategoryDocument() != null
                                && uq.getQuiz().getCategoryDocument().getId().equals(id);
                    return false;
                })
                .map(uq -> quizMapper.toDto(uq.getQuiz()))
                .toList();

        if (quizDtos.isEmpty()) {
            throw new BaseException(CommonResponseCode.NOT_FOUND);
        }

        return HistoryQuizListResponse.builder()
                .createdAt(startOfDay)
                .quizzes(quizDtos)
                .build();
    }
}
