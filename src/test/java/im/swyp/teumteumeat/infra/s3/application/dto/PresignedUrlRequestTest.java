package im.swyp.teumteumeat.infra.s3.application.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import im.swyp.teumteumeat.infra.s3.constant.FileType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PresignedUrlRequestTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("fileType이 없는 기존 요청은 USER_PDF로 매핑된다")
    void defaultFileTypeWhenMissing() throws Exception {
        PresignedUrlRequest request = objectMapper.readValue(
                "{\"fileName\":\"1강.pdf\",\"fileSize\":1024}", PresignedUrlRequest.class);

        assertThat(request.fileType()).isEqualTo(FileType.USER_PDF);
    }

    @Test
    @DisplayName("fileType이 null이어도 USER_PDF로 매핑된다")
    void defaultFileTypeWhenNull() throws Exception {
        PresignedUrlRequest request = objectMapper.readValue(
                "{\"fileName\":\"1강.pdf\",\"fileType\":null}", PresignedUrlRequest.class);

        assertThat(request.fileType()).isEqualTo(FileType.USER_PDF);
    }

    @Test
    @DisplayName("fileType을 명시하면 해당 값으로 매핑된다")
    void explicitFileType() throws Exception {
        PresignedUrlRequest request = objectMapper.readValue(
                "{\"fileName\":\"1강.pdf\",\"fileType\":\"USER_PDF\"}", PresignedUrlRequest.class);

        assertThat(request.fileType()).isEqualTo(FileType.USER_PDF);
    }
}
