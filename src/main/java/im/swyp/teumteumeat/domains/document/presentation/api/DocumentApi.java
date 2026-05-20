package im.swyp.teumteumeat.domains.document.presentation.api;

import im.swyp.teumteumeat.domains.document.application.dto.request.DocumentCreateRequest;
import im.swyp.teumteumeat.domains.document.application.dto.response.DocumentListResponse;
import im.swyp.teumteumeat.domains.document.application.dto.response.DocumentResponse;
import im.swyp.teumteumeat.domains.document.domain.constant.DocumentResponseCode;
import im.swyp.teumteumeat.global.annotation.swagger.ApiErrorResponseExplanation;
import im.swyp.teumteumeat.global.annotation.swagger.ApiResponseExplanations;
import im.swyp.teumteumeat.global.annotation.swagger.ApiSuccessResponseExplanation;
import im.swyp.teumteumeat.global.common.ApiResponse;
import im.swyp.teumteumeat.global.common.CreatedResponse;
import im.swyp.teumteumeat.global.security.annotation.LoginUser;
import im.swyp.teumteumeat.global.security.dto.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "Document(PDF)", description = "문서 API")
public interface DocumentApi {

        @Operation(summary = "문서 등록",
                   description = """
                                 해당 목표에 문서를 등록합니다. (반환된 documentId를 통해 요약/퀴즈 조회를 진행)
                                 - fileKey : Presigned Url 발급 API에서 응답 받은 필드 사용
                                 """
        )
        @ApiResponseExplanations(success = @ApiSuccessResponseExplanation(responseClass = CreatedResponse.class, description = "등록 성공"))
        ResponseEntity<ApiResponse<CreatedResponse>> uploadDocument(
                        @PathVariable Long goalId,
                        @RequestBody @Valid DocumentCreateRequest request,
                        @Parameter(hidden = true) @LoginUser CustomUserDetails user);

        @Operation(summary = "파일 업로드 텍스트 추출 SSE 구독",
                description = """
                              파일 업로드 후 텍스트 추출이 완료되면 응답을 받습니다.
                              
                              **호출 흐름**
                              - goalId, documentId로 GET /api/v1/goals/{goalId}/documents/{documentId}/sse 호출
                                - Accept: text/event-stream (필수)
                                - Last-Event-ID: (네트워크 유실로 인한 재연결 시) 마지막으로 수신한 이벤트의 id값 전달
                              
                              **Event 스펙**
                              
                              event: connect
                              
                                  { "status": "CONNECTED" }
                              
                              event: document_processing_status
                              
                                  // 순서 상관없이 특정 status이 먼저 반환될 수 있음
                                  { "status": "PENDING" }
                                  { "status": "PROCESSING", "remain_ms": 12000 }
                                  { "status": "COMPLETED" }
                                  { "status": "FAILED", "reason": "TIMEOUT" } // reason : TIMEOUT, SERVER_ERROR, ENCRYPTED_FILE
                              
                              **스트림 예시**
                              
                                  id:111:197:1772782439940
                                  event:connect
                                  data:{ "status": "CONNECTED" }
                              
                                  id:111:197:1772782439943
                                  event:document_processing_status
                                  data:{ "status": "PENDING" }
                              
                                  id:111:197:1772782439946
                                  event:document_processing_status
                                  data:{ "status": "PROCESSING", "remain_ms": 12000 }
                              
                                  id:111:197:1772782439949
                                  event:document_processing_status
                                  data:{ "status": "COMPLETED" }
                              
                              **구현부**
                              - 문서 처리 예상 소요 시간 알림을 받았을 경우 로딩바를 통해 (n% 완료, 몇 초 남음 등 표시) 진행 상황을 표시하여 사용자 경험을 향상시켜 이탈을 방지합니다.
                              - 문서 처리 완료 시 퀴즈 생성을 요청합니다(기존에 30초 대기 후 요청했던 흐름 그대로 진행)
                              """
        )
        SseEmitter subscribe(
                        @PathVariable Long goalId,
                        @PathVariable Long documentId,
                        @Parameter(hidden = true) @LoginUser CustomUserDetails user,
                        @RequestHeader(value = "Last-Event-ID", required = false) String lastEventId,
                        HttpServletResponse response);

        @Operation(summary = "문서 조회",
                   description = """
                                 해당 목표에 등록된 문서 목록을 조회합니다.
                                 - estimateTime : 조회 '시각' 기준 남은 '시간'(ms). status가 PROCESSING일때만 반환됨.
                                 예상보다 처리가 일찍 끝난 경우 COMPLETED로 estimateTime 필드 없이 반환되며, PENDING일 경우에도 estimateTime 필드 없이 반환되며 잠시 후 재요청 필요.
                                 예상보다 시간이 오래 걸리는 경우 estimateTime이 0으로 반환되어 일정 시간 후 재요청이 필요한 경우도 있을 수 있음.
                                 """
        )
        @ApiResponseExplanations(success = @ApiSuccessResponseExplanation(responseClass = DocumentListResponse.class, description = "조회 성공"))
        ResponseEntity<ApiResponse<DocumentListResponse>> getDocuments(
                        @PathVariable Long goalId,
                        @Parameter(hidden = true) @LoginUser CustomUserDetails user);

        @Operation(summary = "문서 단건 조회", description = "특정 문서를 조회합니다.")
        @ApiResponseExplanations(
                success = @ApiSuccessResponseExplanation(responseClass = DocumentResponse.class, description = "조회 성공"),
                errors = {
                        @ApiErrorResponseExplanation(exceptionCode = DocumentResponseCode.class, name = "INVALID_DOCUMENT_GOAL_ASSOCIATION")
                })
        ResponseEntity<ApiResponse<DocumentResponse>> getDocument(
                        @PathVariable Long goalId,
                        @PathVariable Long documentId,
                        @Parameter(hidden = true) @LoginUser CustomUserDetails user);

        @Operation(summary = "문서 목록 삭제", description = "해당 목표의 모든 문서를 삭제합니다.")
        @ApiResponseExplanations(success = @ApiSuccessResponseExplanation(description = "삭제 성공"))
        ResponseEntity<ApiResponse<Void>> deleteDocuments(
                        @PathVariable Long goalId,
                        @Parameter(hidden = true) @LoginUser CustomUserDetails user);

        @Operation(summary = "문서 단건 삭제", description = "특정 문서를 삭제합니다.")
        @ApiResponseExplanations(
                success = @ApiSuccessResponseExplanation(description = "삭제 성공"),
                errors = {
                        @ApiErrorResponseExplanation(exceptionCode = DocumentResponseCode.class, name = "INVALID_DOCUMENT_GOAL_ASSOCIATION")
                })
        ResponseEntity<ApiResponse<Void>> deleteDocument(
                        @PathVariable Long goalId,
                        @PathVariable Long documentId,
                        @Parameter(hidden = true) @LoginUser CustomUserDetails user);
}
