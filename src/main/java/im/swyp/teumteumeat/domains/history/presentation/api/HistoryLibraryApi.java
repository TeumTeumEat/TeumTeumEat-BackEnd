package im.swyp.teumteumeat.domains.history.presentation.api;

import im.swyp.teumteumeat.domains.goal.domain.constant.GoalType;
import im.swyp.teumteumeat.domains.history.application.dto.response.*;
import im.swyp.teumteumeat.global.annotation.swagger.ApiResponseExplanations;
import im.swyp.teumteumeat.global.annotation.swagger.ApiSuccessResponseExplanation;
import im.swyp.teumteumeat.global.common.ApiResponse;
import im.swyp.teumteumeat.global.security.annotation.LoginUser;
import im.swyp.teumteumeat.global.security.dto.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "History", description = "히스토리(학습 기록) API")
public interface HistoryLibraryApi {

        @Operation(summary = "히스토리 캘린더 조회", description = "이번 달의 학습 스탬프와 현재 연속 학습일(Streak)을 조회합니다.")
        @ApiResponseExplanations(success = @ApiSuccessResponseExplanation(responseClass = CalendarResponse.class, description = "조회 성공"))
        ResponseEntity<ApiResponse<CalendarResponse>> getCalendar(
                        @Parameter(description = "조회할 년도 (yyyy)", required = false) @RequestParam(required = false) Integer year,
                        @Parameter(description = "조회할 월 (MM)", required = false) @RequestParam(required = false) Integer month,
                        @Parameter(hidden = true) @LoginUser CustomUserDetails user);

        @Operation(summary = "날짜별 상세 내역 조회", description = "특정 날짜의 학습 내역 목록을 조회합니다.")
        @ApiResponseExplanations(success = @ApiSuccessResponseExplanation(responseClass = DailyHistoryResponse.class, description = "조회 성공 (리스트 반환)"))
        ResponseEntity<ApiResponse<List<DailyHistoryResponse>>> getDailyHistory(
                        @Parameter(description = "조회할 날짜 (yyyy-MM-dd)", required = true) @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                        @Parameter(hidden = true) @LoginUser CustomUserDetails user);

        @Operation(summary = "주제별 내역 조회", description = "전체 학습 기록을 주제(카테고리)별로 그룹화하여 조회합니다.")
        @ApiResponseExplanations(success = @ApiSuccessResponseExplanation(responseClass = TopicHistoryResponse.class, description = "조회 성공 (리스트 반환)"))
        ResponseEntity<ApiResponse<List<TopicHistoryResponse>>> getTopicHistory(
                        @Parameter(hidden = true) @LoginUser CustomUserDetails user);

        @Operation(summary = "상세 내용 보기 (요약)", description = "특정 날짜의 특정 학습 기록에 대한 요약 내용을 조회합니다.")
        @ApiResponseExplanations(success = @ApiSuccessResponseExplanation(responseClass = HistorySummaryResponse.class, description = "조회 성공"))
        ResponseEntity<ApiResponse<HistorySummaryResponse>> getHistorySummary(
                        @Parameter(description = "학습 유형 (DOCUMENT / CATEGORY)", required = true) @PathVariable GoalType type,
                        @Parameter(description = "문서 또는 카테고리 문서 ID", required = true) @PathVariable Long id,
                        @Parameter(description = "조회할 날짜 (yyyy-MM-dd)", required = true) @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                        @Parameter(hidden = true) @LoginUser CustomUserDetails user);

        @Operation(summary = "상세 내용 보기 (퀴즈 목록)", description = "특정 날짜의 특정 학습 기록에 대한 퀴즈 목록을 조회합니다.")
        @ApiResponseExplanations(success = @ApiSuccessResponseExplanation(responseClass = HistoryQuizListResponse.class, description = "조회 성공"))
        ResponseEntity<ApiResponse<HistoryQuizListResponse>> getHistoryQuizzes(
                        @Parameter(description = "학습 유형 (DOCUMENT / CATEGORY)", required = true) @PathVariable GoalType type,
                        @Parameter(description = "문서 또는 카테고리 문서 ID", required = true) @PathVariable Long id,
                        @Parameter(description = "조회할 날짜 (yyyy-MM-dd)", required = true) @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                        @Parameter(hidden = true) @LoginUser CustomUserDetails user);
}
