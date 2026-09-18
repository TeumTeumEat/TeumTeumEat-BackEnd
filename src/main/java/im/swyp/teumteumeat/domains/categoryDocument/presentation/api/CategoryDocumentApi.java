package im.swyp.teumteumeat.domains.categoryDocument.presentation.api;

import im.swyp.teumteumeat.domains.categoryDocument.application.dto.response.CategoryDocumentResponse;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "CategoryDocument", description = "카테고리 자료(요약글) API")
public interface CategoryDocumentApi {

        @Operation(summary = "일일 카테고리 요약글 생성 (학습 시작)",
                   description = """
                                 오늘 학습할 새로운 카테고리 요약글과 퀴즈를 동기 방식으로 생성합니다.
                                 
                                 **참고사항**
                                 - 해당 API는 요청 완료까지 대기 시간이 발생할 수 있습니다.
                                 - 성공 시 생성된 카테고리 문서 정보를 반환합니다.
                                 - 요약글을 매일 생성하기 위해, 매일 카테고리 자료(요약글) 생성하기(POST)를 호출 해야 합니다.
                                 """
        )
        @ApiResponseExplanations(
                success = @ApiSuccessResponseExplanation(responseClass = CategoryDocumentResponse.class, description = "생성 성공"),
                errors = {
                        @ApiErrorResponseExplanation(exceptionCode = CommonResponseCode.class, name = "NOT_FOUND"),
                        @ApiErrorResponseExplanation(exceptionCode = GoalResponseCode.class, name = "GOAL_COMPLETED"),
                        @ApiErrorResponseExplanation(exceptionCode = QuizResponseCode.class, name = "TODAY_QUOTA_EXCEEDED"),
                        @ApiErrorResponseExplanation(exceptionCode = QuizResponseCode.class, name = "UNSOLVED_QUIZ_EXISTS")
                })
        ResponseEntity<ApiResponse<CategoryDocumentResponse>> generateDocument(
                        @PathVariable Long categoryId,
                        @Parameter(hidden = true) @LoginUser CustomUserDetails user);

        @Operation(summary = "일일 카테고리 요약글 생성 스트리밍 (학습 시작)",
                   description = """
                                 오늘 학습할 새로운 카테고리 요약글과 퀴즈를 생성합니다.
                                 
                                 **특징**
                                 - 요청 접수 후 비동기로 생성되며 SSE(Server-Sent Events)로 생성 과정이 전달됩니다.
                                 - 클라이언트에서는 SSE 스트림을 구독하여 생성 상태를 사용자에게 로딩바 등으로 표시할 수 있습니다.
                                 - Accept 헤더를 `text/event-stream`으로 설정해야 합니다.
                                 - 요약글이 완성될 때까지 텍스트 청크 단위로 분할되어 연속적으로 스트리밍됩니다. (프록시 버퍼링 방지를 위해 응답 헤더에 'X-Accel-Buffering: no'가 포함됩니다.)
                                 """
        )
        @ApiResponseExplanations(
                success = @ApiSuccessResponseExplanation(description = "생성 요청 접수 성공 " +
                        "(결과는 SSE: connect(Stream 연결 시작) → message(한 글자씩 응답이 내려옴) → title(제목 덩어리))"),
                errors = {
                        @ApiErrorResponseExplanation(exceptionCode = CommonResponseCode.class, name = "NOT_FOUND"),
                        @ApiErrorResponseExplanation(exceptionCode = GoalResponseCode.class, name = "GOAL_COMPLETED"),
                        @ApiErrorResponseExplanation(exceptionCode = QuizResponseCode.class, name = "TODAY_QUOTA_EXCEEDED"),
                        @ApiErrorResponseExplanation(exceptionCode = QuizResponseCode.class, name = "UNSOLVED_QUIZ_EXISTS")
                })
        ResponseEntity<SseEmitter> generateDocumentStream(
                        @PathVariable Long categoryId,
                        @Parameter(hidden = true) @LoginUser CustomUserDetails user);

        @Operation(summary = "일일 카테고리 요약글 단순 조회 (이어 읽기)",
                   description = """
                                 유저가 최근에 발급받아 현재 진행 중인 카테고리 요약글을 조회합니다.
                                 
                                 **특징**
                                 - 퀴즈 풀이 횟수가 차감되지 않습니다.
                                 - 이미 발급된 문서를 그대로 다시 가져오므로 이어 읽기에 활용됩니다.
                                 """
        )
        @ApiResponseExplanations(
                success = @ApiSuccessResponseExplanation(responseClass = CategoryDocumentResponse.class, description = "조회 성공"),
                errors = {
                        @ApiErrorResponseExplanation(exceptionCode = CommonResponseCode.class, name = "NOT_FOUND")
                })
        ResponseEntity<ApiResponse<CategoryDocumentResponse>> getDailyDocument(
                        @PathVariable Long categoryId,
                        @Parameter(hidden = true) @LoginUser CustomUserDetails user);

        @Operation(summary = "유저 맞춤 카테고리 자료(요약글) 생성",
                   description = """
                                 유저의 Goal 프롬프트에 기반하여 맞춤형 카테고리 자료를 생성합니다.
                                 
                                 **권한**
                                 - 관리자(ADMIN) 전용 API입니다.
                                 """
        )
        @ApiResponseExplanations(success = @ApiSuccessResponseExplanation(description = "생성 성공"))
        ResponseEntity<ApiResponse<Void>> createDocument(
                        @PathVariable Long categoryId,
                        @Parameter(hidden = true) @LoginUser CustomUserDetails user);

        @Operation(summary = "전체 카테고리 자료(요약글) 삭제",
                   description = """
                                 등록된 전체 카테고리 자료(요약글)를 삭제합니다.
                                 
                                 **권한**
                                 - 관리자(ADMIN) 전용 API입니다.
                                 """
        )
        @ApiResponseExplanations(success = @ApiSuccessResponseExplanation(description = "삭제 성공"))
        ResponseEntity<ApiResponse<Void>> deleteDocument(
                        @PathVariable Long documentId);
}
