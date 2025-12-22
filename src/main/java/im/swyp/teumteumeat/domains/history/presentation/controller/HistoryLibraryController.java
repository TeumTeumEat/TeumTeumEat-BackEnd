package im.swyp.teumteumeat.domains.history.presentation.controller;

import im.swyp.teumteumeat.domains.goal.domain.constant.GoalType;
import im.swyp.teumteumeat.domains.history.application.dto.response.*;
import im.swyp.teumteumeat.domains.history.application.usecase.HistoryLibraryUseCase;
import im.swyp.teumteumeat.global.common.ApiResponse;
import im.swyp.teumteumeat.global.common.CommonResponseCode;
import im.swyp.teumteumeat.global.security.dto.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/history")
@RequiredArgsConstructor
public class HistoryLibraryController {

    private final HistoryLibraryUseCase historyLibraryUseCase;

    // 히스토리 캘린더 (월간 스탬프, 스트릭)
    @GetMapping("/calendar")
    public ResponseEntity<ApiResponse<CalendarResponse>> getCalendar(
            @AuthenticationPrincipal CustomUserDetails user) {
        CalendarResponse response = historyLibraryUseCase.getCalendar(user.getUserId());
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, response));
    }

    // 날짜별 상세 내역
    @GetMapping("/date/{date}")
    public ResponseEntity<ApiResponse<List<DailyHistoryResponse>>> getDailyHistory(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal CustomUserDetails user) {
        List<DailyHistoryResponse> response = historyLibraryUseCase.getDailyHistory(user.getUserId(), date);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, response));
    }

    // 주제별 내역
    @GetMapping("/topics")
    public ResponseEntity<ApiResponse<List<TopicHistoryResponse>>> getTopicHistory(
            @AuthenticationPrincipal CustomUserDetails user) {
        List<TopicHistoryResponse> response = historyLibraryUseCase.getTopicHistory(user.getUserId());
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, response));
    }

    // 상세 내용 보기 (요약)
    @GetMapping("/details/summary/{type}/{id}")
    public ResponseEntity<ApiResponse<HistorySummaryResponse>> getHistorySummary(
            @PathVariable GoalType type,
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal CustomUserDetails user) {
        HistorySummaryResponse response = historyLibraryUseCase.getHistorySummary(user.getUserId(), type, id, date);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, response));
    }

    // 상세 내용 보기 (퀴즈 목록)
    @GetMapping("/details/quizzes/{type}/{id}")
    public ResponseEntity<ApiResponse<HistoryQuizListResponse>> getHistoryQuizzes(
            @PathVariable GoalType type,
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal CustomUserDetails user) {
        HistoryQuizListResponse response = historyLibraryUseCase.getHistoryQuizzes(user.getUserId(), type, id, date);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, response));
    }
}
