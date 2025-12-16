package im.swyp.teumteumeat.domains.llm.presentation;

import im.swyp.teumteumeat.domains.llm.application.dto.request.LLMRequest;
import im.swyp.teumteumeat.domains.llm.application.dto.response.LLMResponse;
import im.swyp.teumteumeat.domains.llm.domain.service.LLMService;
import im.swyp.teumteumeat.global.common.ApiResponse;
import im.swyp.teumteumeat.global.common.CommonResponseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/llm")
public class LLMController {

    private final LLMService llmService;

    @GetMapping("/quiz/generate")
    public ResponseEntity<ApiResponse<LLMResponse>> createContent(@RequestBody LLMRequest request) {
        LLMResponse answer = llmService.generateAnswer(request);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, answer));
    }
}
