package im.swyp.teumteumeat.domains.quiz.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
public record QuizListResponse(List<QuizDto> quizzes) {
    @Builder
    public record QuizDto(

            @Schema(description = "퀴즈 ID", example = "1")
            Long quizId,

            @Schema(description = "질문", example = "IoC는 객체의 생성과 관리를 Spring 프레임워크가 담당하는 개념이다.")
            String question,

            @Schema(description = "퀴즈 선지")
            List<String> options,

            @Schema(description = "질문", example = "O (OX의 경우), 의존성 증가(객관식의 경우))")
            String answer,

            @Schema(description = "퀴즈 유형", example = "OX/MCQ")
            String type,

            @Schema(description = "해설", example = "Ioc는...")
            String explanation
    ) {
    }
}
