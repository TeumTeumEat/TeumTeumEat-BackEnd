package im.swyp.teumteumeat.domains.document.presentation.api;

import im.swyp.teumteumeat.domains.document.application.dto.request.DocumentCreateRequest;
import im.swyp.teumteumeat.domains.document.application.dto.response.DocumentListResponse;
import im.swyp.teumteumeat.domains.document.application.dto.response.DocumentResponse;
import im.swyp.teumteumeat.global.annotation.swagger.ApiResponseExplanations;
import im.swyp.teumteumeat.global.annotation.swagger.ApiSuccessResponseExplanation;
import im.swyp.teumteumeat.global.common.ApiResponse;
import im.swyp.teumteumeat.global.common.CreatedResponse;
import im.swyp.teumteumeat.global.security.dto.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "Document(PDF)", description = "문서 API")
public interface DocumentApi {

        @Operation(summary = "문서 등록", description = "해당 목표에 문서를 등록합니다. (반환된 documentId를 통해 요약/퀴즈 조회를 진행)")
        @ApiResponseExplanations(success = @ApiSuccessResponseExplanation(responseClass = CreatedResponse.class, description = "등록 성공"))
        ResponseEntity<ApiResponse<CreatedResponse>> uploadDocument(
                        @PathVariable Long goalId,
                        @RequestBody @Valid DocumentCreateRequest request,
                        @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user);

        @Operation(summary = "파일 업로드 텍스트 추출 SSE 구독", description = "파일 업로드 후 텍스트 추출이 완료되면 응답을 받습니다. (자세한 설명은 Notion API 명세서 참조)")
        SseEmitter subscribe(
                        @PathVariable Long goalId,
                        @PathVariable Long documentId,
                        @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user,
                        @RequestHeader(value = "Last-Event-ID", required = false) String lastEventId,
                        HttpServletResponse response);

        @Operation(summary = "문서 조회", description = "해당 목표에 등록된 문서 목록을 조회합니다.")
        @ApiResponseExplanations(success = @ApiSuccessResponseExplanation(responseClass = DocumentListResponse.class, description = "조회 성공"))
        ResponseEntity<ApiResponse<DocumentListResponse>> getDocuments(
                        @PathVariable Long goalId,
                        @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user);

        @Operation(summary = "문서 단건 조회", description = "특정 문서를 조회합니다.")
        @ApiResponseExplanations(success = @ApiSuccessResponseExplanation(responseClass = DocumentResponse.class, description = "조회 성공"))
        ResponseEntity<ApiResponse<DocumentResponse>> getDocument(
                        @PathVariable Long goalId,
                        @PathVariable Long documentId,
                        @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user);

        @Operation(summary = "문서 목록 삭제", description = "해당 목표의 모든 문서를 삭제합니다.")
        @ApiResponseExplanations(success = @ApiSuccessResponseExplanation(description = "삭제 성공"))
        ResponseEntity<ApiResponse<Void>> deleteDocuments(
                        @PathVariable Long goalId,
                        @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user);

        @Operation(summary = "문서 단건 삭제", description = "특정 문서를 삭제합니다.")
        @ApiResponseExplanations(success = @ApiSuccessResponseExplanation(description = "삭제 성공"))
        ResponseEntity<ApiResponse<Void>> deleteDocument(
                        @PathVariable Long goalId,
                        @PathVariable Long documentId,
                        @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user);
}
