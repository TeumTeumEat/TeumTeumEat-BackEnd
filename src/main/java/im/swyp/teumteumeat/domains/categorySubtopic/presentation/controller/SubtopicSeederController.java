package im.swyp.teumteumeat.domains.categorySubtopic.presentation.controller;

import im.swyp.teumteumeat.domains.categorySubtopic.application.usecase.SubtopicSeederUseCase;
import im.swyp.teumteumeat.domains.categorySubtopic.presentation.api.SubtopicSeederApi;
import im.swyp.teumteumeat.global.common.ApiResponse;
import im.swyp.teumteumeat.global.common.CommonResponseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/subtopics")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class SubtopicSeederController implements SubtopicSeederApi {

    private final SubtopicSeederUseCase subtopicSeederUseCase;

    @Override
    @PostMapping("/seed")
    public ResponseEntity<ApiResponse<String>> seedSubtopics(
            @RequestParam Long startId,
            @RequestParam Long endId,
            @RequestParam(defaultValue = "false") boolean overwrite
    ) {
        int count = subtopicSeederUseCase.seed(startId, endId, overwrite);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, "소주제 시딩 완료. 생성된 항목 수: " + count));
    }
}
