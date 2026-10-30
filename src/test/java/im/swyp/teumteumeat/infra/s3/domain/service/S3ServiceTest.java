package im.swyp.teumteumeat.infra.s3.domain.service;

import im.swyp.teumteumeat.global.config.properties.S3Properties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.core.env.Environment;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class S3ServiceTest {

    private static final String KEY = "dev/origin/uuid_1강.pdf";

    private S3Service s3Service(String baseUrl) {
        S3Properties properties = new S3Properties("teumteumeat", "ap-northeast-2", baseUrl);
        return new S3Service(mock(S3Presigner.class), properties, mock(Environment.class));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  "})
    @DisplayName("base-url이 비어 있으면 S3 기본 URL을 사용한다")
    void generateFileUrlWithoutBaseUrl(String baseUrl) {
        assertThat(s3Service(baseUrl).generateFileUrl(KEY))
                .isEqualTo("https://teumteumeat.s3.ap-northeast-2.amazonaws.com/" + KEY);
    }

    @ParameterizedTest
    @ValueSource(strings = {"https://cdn.teumteumeat.co.kr", "https://cdn.teumteumeat.co.kr/"})
    @DisplayName("base-url이 설정돼 있으면 끝 슬래시 유무와 관계없이 해당 URL을 사용한다")
    void generateFileUrlWithBaseUrl(String baseUrl) {
        assertThat(s3Service(baseUrl).generateFileUrl(KEY))
                .isEqualTo("https://cdn.teumteumeat.co.kr/" + KEY);
    }
}
