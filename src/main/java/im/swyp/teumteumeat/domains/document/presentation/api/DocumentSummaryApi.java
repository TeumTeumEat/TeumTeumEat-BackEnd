package im.swyp.teumteumeat.domains.document.presentation.api;

import im.swyp.teumteumeat.domains.document.application.dto.response.DocumentDetailResponse;
import im.swyp.teumteumeat.domains.document.domain.constant.DocumentResponseCode;
import im.swyp.teumteumeat.domains.goal.domain.constant.GoalResponseCode;
import im.swyp.teumteumeat.domains.quiz.domain.constant.QuizResponseCode;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "Document Summary(PDF)", description = "PDF 요약 API")
@RequestMapping("/api/v1/goals/{goalId}/documents")
public interface DocumentSummaryApi {

        @Operation(summary = "PDF 요약글 및 퀴즈 생성 (학습 시작)",
                   description = """
                                 업로드된 PDF 문서에 대한 새로운 요약본과 퀴즈를 동기 방식으로 생성합니다.
                                 
                                 **참고사항**
                                 - 요청 완료까지 대기 시간이 발생할 수 있습니다.
                                 - 성공 시 생성된 요약 및 퀴즈 상세 정보를 반환합니다.
                                 - 요약글을 매일 생성하기 위해, 매일 카테고리 자료(요약글) 생성하기(POST)를 호출 해야 합니다.
                                 """
        )
        @ApiResponseExplanations(
                success = @ApiSuccessResponseExplanation(responseClass = DocumentDetailResponse.class, description = "생성 요청 성공"),
                errors = {
                        @ApiErrorResponseExplanation(exceptionCode = GoalResponseCode.class, name = "GOAL_COMPLETED"),
                        @ApiErrorResponseExplanation(exceptionCode = GoalResponseCode.class, name = "GOAL_EXPIRED"),
                        @ApiErrorResponseExplanation(exceptionCode = QuizResponseCode.class, name = "TODAY_QUOTA_EXCEEDED"),
                        @ApiErrorResponseExplanation(exceptionCode = QuizResponseCode.class, name = "UNSOLVED_QUIZ_EXISTS"),
                        @ApiErrorResponseExplanation(exceptionCode = CommonResponseCode.class, name = "NOT_FOUND")
                })
        ResponseEntity<ApiResponse<DocumentDetailResponse>> createSummary(
                        @PathVariable Long goalId,
                        @PathVariable Long documentId,
                        @Parameter(hidden = true) @LoginUser CustomUserDetails user);

        @Operation(summary = "스트리밍 방식 - PDF 요약글 및 퀴즈 생성 (학습 시작)",
                   description = """
                                 업로드된 PDF 문서에 대한 새로운 요약본과 퀴즈를 생성하고 과정을 스트리밍합니다.
                                 
                                 **특징**
                                 - 비동기로 처리되며 SSE(Server-Sent Events)를 통해 상태가 전달됩니다.
                                 - 클라이언트는 `text/event-stream`으로 응답을 받아 로딩 UI에 활용할 수 있습니다.
                                 - 요약글이 완성될 때까지 텍스트 청크 단위로 분할되어 연속적으로 스트리밍됩니다. (프록시 버퍼링 방지를 위해 응답 헤더에 'X-Accel-Buffering: no'가 포함됩니다.)
                                 """
        )
        @ApiResponseExplanations(
                success = @ApiSuccessResponseExplanation(responseClass = DocumentDetailResponse.class, description = "생성 요청 성공"),
                errors = {
                        @ApiErrorResponseExplanation(exceptionCode = GoalResponseCode.class, name = "GOAL_COMPLETED"),
                        @ApiErrorResponseExplanation(exceptionCode = GoalResponseCode.class, name = "GOAL_EXPIRED"),
                        @ApiErrorResponseExplanation(exceptionCode = QuizResponseCode.class, name = "TODAY_QUOTA_EXCEEDED"),
                        @ApiErrorResponseExplanation(exceptionCode = QuizResponseCode.class, name = "UNSOLVED_QUIZ_EXISTS"),
                        @ApiErrorResponseExplanation(exceptionCode = CommonResponseCode.class, name = "NOT_FOUND")
                })
        ResponseEntity<SseEmitter> createSummaryStream(
                @PathVariable Long goalId,
                @PathVariable Long documentId,
                @Parameter(hidden = true) @LoginUser CustomUserDetails user);

        @Operation(summary = "PDF 요약글 단순 조회 (이어 읽기)",
                   description = """
                                 가장 최근에 생성되어 현재 진행 중인 PDF 요약글을 조회합니다.
                                 
                                 **특징**
                                 - 퀴즈 풀이 횟수가 차감되지 않고 문서를 다시 불러올 수 있습니다.
                                 - DOCUMENT_NOT_READY 에러 발생 시 아직 요약이 완료되지 않은 상태입니다.
                                 """
        )
        @ApiResponseExplanations(
                success = @ApiSuccessResponseExplanation(responseClass = DocumentDetailResponse.class, description = "조회 성공"),
                errors = {
                        @ApiErrorResponseExplanation(exceptionCode = DocumentResponseCode.class, name = "DOCUMENT_NOT_READY"),
                        @ApiErrorResponseExplanation(exceptionCode = CommonResponseCode.class, name = "NOT_FOUND")
                })
        ResponseEntity<ApiResponse<DocumentDetailResponse>> getSummary(
                        @PathVariable Long goalId,
                        @PathVariable Long documentId,
                        @Parameter(hidden = true) @LoginUser CustomUserDetails user);
}
