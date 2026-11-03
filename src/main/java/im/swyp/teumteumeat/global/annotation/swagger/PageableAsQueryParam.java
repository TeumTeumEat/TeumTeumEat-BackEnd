package im.swyp.teumteumeat.global.annotation.swagger;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/* org.springdoc.core.converters.models.PageableAsQueryParam 한글 버전 재정의, 정렬은 서버에서 고정하므로 sort 제외 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Parameters({
    @Parameter(
        in = ParameterIn.QUERY,
        description = "0부터 시작하는 페이지 번호 (0..N)",
        name = "page",
        schema = @Schema(type = "integer", defaultValue = "0")
    ),
    @Parameter(
        in = ParameterIn.QUERY,
        description = "한 페이지에 포함될 항목 수",
        name = "size",
        schema = @Schema(type = "integer", defaultValue = "20")
    )
})
public @interface PageableAsQueryParam {
}
