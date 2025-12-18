package im.swyp.teumteumeat.global.config.swagger;

import im.swyp.teumteumeat.global.annotation.swagger.ApiErrorResponseExplanation;
import im.swyp.teumteumeat.global.annotation.swagger.ApiResponseExplanations;
import im.swyp.teumteumeat.global.common.BaseResponseCode;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import lombok.Builder;
import lombok.Getter;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ApiErrorResponseHandler {

    public void handleApiErrorResponse(
            Operation operation,
            HandlerMethod handlerMethod
    ) {
        ApiResponseExplanations apiResponseExplanations = handlerMethod.getMethodAnnotation(ApiResponseExplanations.class);

        if (apiResponseExplanations != null) {
            generateResponseCodeResponseExample(operation, Arrays.asList(apiResponseExplanations.errors()));
        }
    }

    private void generateResponseCodeResponseExample(
            Operation operation,
            List<ApiErrorResponseExplanation> apiErrorResponseExamples
    ) {
        ApiResponses responses = operation.getResponses();

        Map<Integer, List<ExampleHolder>> statusWithExampleHolders = apiErrorResponseExamples.stream()
                .map(this::createExampleHolder)
                .collect(Collectors.groupingBy(ExampleHolder::getHttpStatusCode));

        addExamplesToResponses(responses, statusWithExampleHolders);
    }

    private ExampleHolder createExampleHolder(ApiErrorResponseExplanation apiErrorResponseExample) {
        Class<? extends BaseResponseCode> enumClass = apiErrorResponseExample.exceptionCode();
        BaseResponseCode[] codes = enumClass.getEnumConstants();

        if (codes != null && codes.length > 0) {
            BaseResponseCode responseCode = codes[0];

            return ExampleHolder.builder()
                    .httpStatusCode(responseCode.getStatus().value())
                    .name(((Enum<?>) responseCode).name())
                    .errorCode(responseCode.getCode())
                    .description(responseCode.getMessage())
                    .holder(createSwaggerExample(responseCode, responseCode.getMessage()))
                    .build();
        }

        return null;
    }

    private Example createSwaggerExample(BaseResponseCode responseCode, String description) {
        im.swyp.teumteumeat.global.common.ApiResponse<Object> apiResponse
                = im.swyp.teumteumeat.global.common.ApiResponse.ofFail(responseCode);

        Example example = new Example();
        example.setValue(apiResponse);
        example.setDescription(description); // 설명을 예제에 추가

        return example;
    }

    private void addExamplesToResponses(
            ApiResponses responses,
            Map<Integer, List<ExampleHolder>> statusWithExampleHolders
    ) {
        statusWithExampleHolders.forEach((status, exampleHolders) -> {
            Content content = new Content();
            MediaType mediaType = new MediaType();
            ApiResponse apiResponse = new ApiResponse();

            exampleHolders.forEach(
                    exampleHolder -> mediaType.addExamples(exampleHolder.getName(), exampleHolder.getHolder())
            );

            content.addMediaType("application/json", mediaType);
            apiResponse.setContent(content);
            responses.addApiResponse(String.valueOf(status), apiResponse);
        });
    }

    @Getter
    @Builder
    public static class ExampleHolder {
        private final int httpStatusCode;
        private final String name;
        private final String errorCode;
        private final String description;
        private final Example holder;
    }
}
