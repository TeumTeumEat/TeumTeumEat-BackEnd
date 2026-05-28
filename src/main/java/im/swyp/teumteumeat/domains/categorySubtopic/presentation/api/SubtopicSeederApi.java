package im.swyp.teumteumeat.domains.categorySubtopic.presentation.api;

import im.swyp.teumteumeat.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Admin - Subtopic Seeding API", description = "카테고리 소주제 사전 생성 API")
public interface SubtopicSeederApi {

    @Operation(
            summary = "소주제 대량 생성",
            description = "지정된 카테고리 범위에 대해 기간별(1~4주) 소주제 목록을 LLM으로 생성합니다."
    )
    ResponseEntity<ApiResponse<String>> seedSubtopics(
            @Parameter(description = "시작 카테고리 ID") @RequestParam Long startId,
            @Parameter(description = "종료 카테고리 ID") @RequestParam Long endId,
            @Parameter(description = "이미 존재하는 경우 덮어쓸지 여부") @RequestParam(defaultValue = "false") boolean overwrite
    );
}
