package im.swyp.teumteumeat.domains.history.presentation.api;

import im.swyp.teumteumeat.domains.goal.domain.constant.GoalType;
import im.swyp.teumteumeat.domains.history.application.dto.response.*;
import im.swyp.teumteumeat.global.annotation.swagger.ApiErrorResponseExplanation;
import im.swyp.teumteumeat.global.annotation.swagger.ApiResponseExplanations;
import im.swyp.teumteumeat.global.annotation.swagger.ApiSuccessResponseExplanation;
import im.swyp.teumteumeat.global.common.ApiResponse;
import im.swyp.teumteumeat.global.common.CommonResponseCode;
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

        @Operation(summary = "히스토리 캘린더 조회",
                   description = """
                                 선택한 연도/월에 해당하는 학습 스탬프 내역과 현재 연속 학습일(Streak) 정보를 조회합니다.
                                 
                                 **파라미터**
                                 - year, month: 조회할 년/월. 값을 넘기지 않으면 현재 날짜 기준으로 조회됩니다.
                                 """
        )
        @ApiResponseExplanations(success = @ApiSuccessResponseExplanation(responseClass = CalendarResponse.class, description = "조회 성공"))
        ResponseEntity<ApiResponse<CalendarResponse>> getCalendar(
                        @Parameter(description = "조회할 년도 (yyyy)", required = false) @RequestParam(required = false) Integer year,
                        @Parameter(description = "조회할 월 (MM)", required = false) @RequestParam(required = false) Integer month,
                        @Parameter(hidden = true) @LoginUser CustomUserDetails user);

        @Operation(summary = "날짜별 상세 내역 조회",
                   description = """
                                 특정 날짜에 진행한 모든 학습 내역 목록을 조회합니다.
                                 
                                 **파라미터**
                                 - date: 조회할 날짜 (형식: yyyy-MM-dd)
                                 """
        )
        @ApiResponseExplanations(success = @ApiSuccessResponseExplanation(responseClass = DailyHistoryResponse.class, description = "조회 성공 (리스트 반환)"))
        ResponseEntity<ApiResponse<List<DailyHistoryResponse>>> getDailyHistory(
                        @Parameter(description = "조회할 날짜 (yyyy-MM-dd)", required = true) @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                        @Parameter(hidden = true) @LoginUser CustomUserDetails user);

        @Operation(summary = "주제별 내역 조회",
                   description = """
                                 사용자의 전체 학습 기록을 주제(카테고리)별로 그룹화하여 조회합니다.
                                 
                                 **특징**
                                 - 각 카테고리별 누적 학습 정보를 한눈에 파악할 수 있습니다.
                                 """
        )
        @ApiResponseExplanations(success = @ApiSuccessResponseExplanation(responseClass = TopicHistoryResponse.class, description = "조회 성공 (리스트 반환)"))
        ResponseEntity<ApiResponse<List<TopicHistoryResponse>>> getTopicHistory(
                        @Parameter(hidden = true) @LoginUser CustomUserDetails user);

        @Operation(summary = "상세 내용 보기 (요약)",
                   description = """
                                 특정 날짜에 수행한 단일 학습 기록의 '요약글' 상세 내용을 조회합니다.
                                 
                                 **파라미터**
                                 - type: 목표의 학습 유형 (DOCUMENT / CATEGORY)
                                 - id: 해당 학습의 원본 문서 또는 카테고리 문서 ID
                                 - date: 학습을 수행한 날짜 (형식: yyyy-MM-dd)
                                 """
        )
        @ApiResponseExplanations(
                success = @ApiSuccessResponseExplanation(responseClass = HistorySummaryResponse.class, description = "조회 성공"),
                errors = {
                        @ApiErrorResponseExplanation(exceptionCode = CommonResponseCode.class, name = "NOT_FOUND")
                })
        ResponseEntity<ApiResponse<HistorySummaryResponse>> getHistorySummary(
                        @Parameter(description = "학습 유형 (DOCUMENT / CATEGORY)", required = true) @PathVariable GoalType type,
                        @Parameter(description = "문서 또는 카테고리 문서 ID", required = true) @PathVariable Long id,
                        @Parameter(description = "조회할 날짜 (yyyy-MM-dd)", required = true) @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                        @Parameter(hidden = true) @LoginUser CustomUserDetails user);

        @Operation(summary = "상세 내용 보기 (퀴즈 목록)",
                   description = """
                                 특정 날짜에 수행한 단일 학습 기록에 포함된 '퀴즈 목록'과 풀이 결과를 조회합니다.
                                 
                                 **파라미터**
                                 - type: 목표의 학습 유형 (DOCUMENT / CATEGORY)
                                 - id: 해당 학습의 원본 문서 또는 카테고리 문서 ID
                                 - date: 학습을 수행한 날짜 (형식: yyyy-MM-dd)
                                 """
        )
        @ApiResponseExplanations(
                success = @ApiSuccessResponseExplanation(responseClass = HistoryQuizListResponse.class, description = "조회 성공"),
                errors = {
                        @ApiErrorResponseExplanation(exceptionCode = CommonResponseCode.class, name = "NOT_FOUND")
                })
        ResponseEntity<ApiResponse<HistoryQuizListResponse>> getHistoryQuizzes(
                        @Parameter(description = "학습 유형 (DOCUMENT / CATEGORY)", required = true) @PathVariable GoalType type,
                        @Parameter(description = "문서 또는 카테고리 문서 ID", required = true) @PathVariable Long id,
                        @Parameter(description = "조회할 날짜 (yyyy-MM-dd)", required = true) @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                        @Parameter(hidden = true) @LoginUser CustomUserDetails user);
}
