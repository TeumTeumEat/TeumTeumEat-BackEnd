package im.swyp.teumteumeat.global.config.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;

@Configuration
@RequiredArgsConstructor
public class SwaggerConfig {

    @Value("${swagger.server-url}")
    private String serverUrl;
    private final ApiSuccessResponseHandler apiSuccessResponseHandler;
    private final ApiErrorResponseHandler apiErrorResponseHandler;

    @Bean
    public OpenAPI openAPI() {
        String jwtSchemeName = "jwtAuth";
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);
        Components components = new Components()
                .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                        .name(jwtSchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("Bearer")
                        .bearerFormat("JWT"));

        return new OpenAPI()
                .components(components)
                .addServersItem(new Server().url(serverUrl))
                .addSecurityItem(securityRequirement)
                .info(new Info()
                        .title("TeumTeumEat API")
                        .description("틈틈잇 백엔드 API 문서\n\n[📋 에러 코드 명세 보기](" + serverUrl + "/error-codes)")
                        .version("v1.0"));
    }

    @Bean
    public OperationCustomizer customize() {
        return (Operation operation, HandlerMethod handlerMethod) -> {
            apiSuccessResponseHandler.handleApiSuccessResponse(operation, handlerMethod);
            apiErrorResponseHandler.handleApiErrorResponse(operation, handlerMethod);
            return operation;
        };
    }
}