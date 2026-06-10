package im.swyp.teumteumeat.domains.quiz.presentation.api;

import im.swyp.teumteumeat.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Test & Seeding API", description = "초기 데이터 구축 및 테스트용 API")
@Deprecated
public interface QuizSeederApi {

        @Operation(summary = "요약글(문서) 대량 생성",
                   deprecated = true,
                   description = """
                                 지정된 카테고리 범위에 대해 '전반적인 내용' 요약글을 생성합니다.
                                 
                                 **특징**
                                 - Goal과 연결되지 않은 템플릿용 문서가 생성됩니다.
                                 - 초기 데이터 구축 및 테스트 용도로 사용됩니다.
                                 """
        )
        ResponseEntity<ApiResponse<String>> seedDocuments(
                        @Parameter(description = "시작 카테고리 ID") @RequestParam Long startId,
                        @Parameter(description = "종료 카테고리 ID") @RequestParam Long endId,
                        @Parameter(description = "카테고리 당 생성할 문서 개수") @RequestParam(defaultValue = "1") int count);

        @Operation(summary = "퀴즈 대량 생성",
                   deprecated = true,
                   description = """
                                 생성된 요약글(Goal이 없는 템플릿 문서)을 기반으로 난이도별 퀴즈를 대량 생성합니다.
                                 
                                 **특징**
                                 - 상/중/하 난이도 각각 5문제씩, 총 15문제가 생성됩니다.
                                 - 초기 데이터 구축 및 테스트 용도로 사용됩니다.
                                 """
        )
        ResponseEntity<ApiResponse<String>> seedQuizzes(
                        @Parameter(description = "시작 카테고리 ID") @RequestParam Long startId,
                        @Parameter(description = "종료 카테고리 ID") @RequestParam Long endId);
}
