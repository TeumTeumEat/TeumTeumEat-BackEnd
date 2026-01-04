package im.swyp.teumteumeat.domains.history.application.usecase;

import im.swyp.teumteumeat.domains.categoryDocument.persistence.entity.CategoryDocument;
import im.swyp.teumteumeat.domains.document.persistence.entity.Document;
import im.swyp.teumteumeat.domains.goal.domain.constant.GoalType;
import im.swyp.teumteumeat.domains.history.application.dto.response.*;
import im.swyp.teumteumeat.domains.quiz.persistence.entity.Quiz;

import im.swyp.teumteumeat.domains.history.application.mapper.HistoryMapper;
import im.swyp.teumteumeat.domains.userQuiz.domain.service.UserQuizService;
import im.swyp.teumteumeat.domains.userQuiz.persistence.entity.UserQuiz;
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

    private final UserQuizService userQuizService;
    private final HistoryMapper historyMapper;

    @Transactional(readOnly = true)
    public CalendarResponse getCalendar(Long userId, Integer year, Integer month) {
        // 기준 날짜 설정 (파라미터가 없으면 현재 시간 기준)
        LocalDateTime now = LocalDateTime.now();
        int targetYear = (year != null) ? year : now.getYear();
        int targetMonth = (month != null) ? month : now.getMonthValue();

        LocalDateTime startOfMonth = LocalDateTime.of(targetYear, targetMonth, 1, 0, 0, 0);
        LocalDateTime endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.toLocalDate().lengthOfMonth())
                .with(LocalTime.MAX);

        List<UserQuiz> monthlyQuizzes = userQuizService.getQuizzesByDateRange(userId, startOfMonth, endOfMonth);

        // 스탬프 날짜 추출 (중복 제거)
        List<LocalDate> stampedDates = monthlyQuizzes.stream()
                .map(uq -> uq.getCreatedDate().toLocalDate())
                .distinct()
                .sorted()
                .toList();

        int currentStreak = userQuizService.calculateCurrentStreak(userId);

        return CalendarResponse.builder()
                .stampedDates(stampedDates)
                .totalStamps(userQuizService.getTotalStudyDays(userId))
                .monthlyStamps(stampedDates.size())
                .currentStreak(currentStreak)
                .build();
    }

    @Transactional(readOnly = true)
    public List<DailyHistoryResponse> getDailyHistory(Long userId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        List<UserQuiz> quizzes = userQuizService.getQuizzesByDateRange(userId, startOfDay, endOfDay);

        // Document/CategoryDocument 단위로 그룹화
        // Set을 사용하여 중복 제거 (같은 문서를 여러 번 풀어도 당일에는 한 번만 표시)
        Set<String> processedKeys = new HashSet<>();
        List<DailyHistoryResponse> result = new ArrayList<>();

        for (UserQuiz uq : quizzes) {
            Quiz quiz = uq.getQuiz();
            String key = null;
            DailyHistoryResponse item = null;

            if (quiz.getDocument() != null) {
                Document doc = quiz.getDocument();
                String title = (quiz.getDocumentSummary() != null && quiz.getDocumentSummary().getTitle() != null)
                        ? quiz.getDocumentSummary().getTitle()
                        : doc.getFileName();
                String summary = (quiz.getDocumentSummary() != null) ? quiz.getDocumentSummary().getSummary() : null;

                key = "DOC_" + doc.getId();
                if (!processedKeys.contains(key)) {
                    item = DailyHistoryResponse.builder()
                            .id(doc.getId())
                            .type(GoalType.DOCUMENT)
                            .title(title) // PDF 파일명 또는 요약 제목
                            .summarySnippet(getSnippet(summary))
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
                            .title(doc.getTitle() != null ? doc.getTitle() : doc.getCategory().getName()) // 카테고리명 또는 요약
                                                                                                          // 제목
                            .summarySnippet(getSnippet(doc.getContent()))
                            .lastStudiedAt(uq.getCreatedDate())
                            .build();
                }
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

        // 마크다운 제거
        String plainText = fullText.replaceAll("[#*_`\\-\\[\\]]", "") // 특수문자 제거
                .replaceAll("\\n+", " ") // 줄바꿈을 공백으로 변경
                .trim();

        return plainText.length() > 50 ? plainText.substring(0, 50) + "..." : plainText;
    }

    @Transactional(readOnly = true)
    public List<TopicHistoryResponse> getTopicHistory(Long userId) {
        // 전체 기록 조회
        List<UserQuiz> allQuizzes = userQuizService.getAllQuizzes(userId);

        Map<String, List<DailyHistoryResponse>> grouped = new HashMap<>();
        Set<String> processedKeys = new HashSet<>();

        for (UserQuiz uq : allQuizzes) {
            Quiz quiz = uq.getQuiz();
            String categoryName = null;
            DailyHistoryResponse item = null;
            String uniqueKey = null;

            // Document(PDF)
            if (quiz.getDocument() != null) {
                Document doc = quiz.getDocument();
                categoryName = doc.getFileName(); // PDF는 카테고리 미정, 파일명으로 대체
                uniqueKey = "DOC_" + doc.getId();

                String title = (quiz.getDocumentSummary() != null && quiz.getDocumentSummary().getTitle() != null)
                        ? quiz.getDocumentSummary().getTitle()
                        : doc.getFileName();
                String summary = (quiz.getDocumentSummary() != null) ? quiz.getDocumentSummary().getSummary() : null;

                if (!processedKeys.contains(uniqueKey)) {
                    item = DailyHistoryResponse.builder()
                            .id(doc.getId())
                            .type(GoalType.DOCUMENT)
                            .title(title)
                            .summarySnippet(getSnippet(summary))
                            .lastStudiedAt(uq.getCreatedDate())
                            .build();
                }

            }

            // Category Document
            else if (quiz.getCategoryDocument() != null) {
                CategoryDocument doc = quiz.getCategoryDocument();
                categoryName = doc.getCategory().getName();
                uniqueKey = "CAT_" + doc.getId();

                if (!processedKeys.contains(uniqueKey)) {
                    item = DailyHistoryResponse.builder()
                            .id(doc.getId())
                            .type(GoalType.CATEGORY)
                            .title(doc.getTitle() != null ? doc.getTitle() : categoryName)
                            .summarySnippet(getSnippet(doc.getContent()))
                            .lastStudiedAt(uq.getCreatedDate())
                            .build();
                }
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

        List<UserQuiz> quizzes = userQuizService.getQuizzesByDateRange(userId, startOfDay, endOfDay);

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
            title = (quiz.getDocumentSummary() != null && quiz.getDocumentSummary().getTitle() != null)
                    ? quiz.getDocumentSummary().getTitle()
                    : quiz.getDocument().getFileName();
            summary = (quiz.getDocumentSummary() != null) ? quiz.getDocumentSummary().getSummary() : null;
        } else {
            title = quiz.getCategoryDocument().getTitle() != null ? quiz.getCategoryDocument().getTitle()
                    : quiz.getCategoryDocument().getCategory().getName();
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

        List<UserQuiz> quizzes = userQuizService.getQuizzesByDateRange(userId, startOfDay, endOfDay);

        List<HistoryQuizListResponse.HistoryQuizDto> quizDtos = quizzes
                .stream()
                .filter(uq -> {
                    if (type == GoalType.DOCUMENT)
                        return uq.getQuiz().getDocument() != null && uq.getQuiz().getDocument().getId().equals(id);
                    if (type == GoalType.CATEGORY)
                        return uq.getQuiz().getCategoryDocument() != null
                                && uq.getQuiz().getCategoryDocument().getId().equals(id);
                    return false;
                })
                .map(historyMapper::toDto)
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
