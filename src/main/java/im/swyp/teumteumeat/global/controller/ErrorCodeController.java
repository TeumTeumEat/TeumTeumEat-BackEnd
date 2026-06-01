package im.swyp.teumteumeat.global.controller;

import im.swyp.teumteumeat.domains.category.domain.constant.CategoryResponseCode;
import im.swyp.teumteumeat.domains.categoryDocument.domain.constant.CategoryDocumentResponseCode;
import im.swyp.teumteumeat.domains.document.domain.constant.DocumentResponseCode;
import im.swyp.teumteumeat.domains.goal.domain.constant.GoalResponseCode;
import im.swyp.teumteumeat.domains.llm.domain.constant.LLMResponseCode;
import im.swyp.teumteumeat.domains.quiz.domain.constant.QuizResponseCode;
import im.swyp.teumteumeat.domains.user.domain.constant.UserResponseCode;
import im.swyp.teumteumeat.global.common.BaseResponseCode;
import im.swyp.teumteumeat.global.common.CommonResponseCode;
import im.swyp.teumteumeat.global.security.constant.AuthResponseCode;
import im.swyp.teumteumeat.infra.s3.constant.FileResponseCode;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Arrays;
import java.util.List;

@Controller
public class ErrorCodeController {

    @GetMapping("/error-codes")
    public String showErrorCodes(Model model) {
        List<ErrorCodeGroup> groups = List.of(
            buildGroup("공통", CommonResponseCode.values()),
            buildGroup("인증", AuthResponseCode.values()),
            buildGroup("유저", UserResponseCode.values()),
            buildGroup("퀴즈", QuizResponseCode.values()),
            buildGroup("목표", GoalResponseCode.values()),
            buildGroup("문서", DocumentResponseCode.values()),
            buildGroup("카테고리", CategoryResponseCode.values()),
            buildGroup("카테고리-문서", CategoryDocumentResponseCode.values()),
            buildGroup("AI/LLM", LLMResponseCode.values()),
            buildGroup("파일", FileResponseCode.values())
        );
        model.addAttribute("groups", groups);
        return "error-codes";
    }

    private <T extends Enum<T> & BaseResponseCode> ErrorCodeGroup buildGroup(String name, T[] values) {
        List<ErrorCodeInfo> codes = Arrays.stream(values)
            .map(v -> new ErrorCodeInfo(
                v.name(),
                v.getStatus().value(),
                v.getStatus().getReasonPhrase(),
                v.getCode(),
                v.getMessage()
            ))
            .toList();
        return new ErrorCodeGroup(name, codes);
    }

    public record ErrorCodeGroup(String name, List<ErrorCodeInfo> codes) {}

    public record ErrorCodeInfo(
        String enumName,
        int httpStatusCode,
        String httpStatusReasonPhrase,
        String code,
        String message
    ) {}
}
