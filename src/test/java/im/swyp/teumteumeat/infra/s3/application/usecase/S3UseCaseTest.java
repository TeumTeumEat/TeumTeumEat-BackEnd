package im.swyp.teumteumeat.infra.s3.application.usecase;

import im.swyp.teumteumeat.global.exception.BaseException;
import im.swyp.teumteumeat.infra.s3.application.dto.PresignedUrlRequest;
import im.swyp.teumteumeat.infra.s3.constant.FileResponseCode;
import im.swyp.teumteumeat.infra.s3.constant.FileType;
import im.swyp.teumteumeat.infra.s3.domain.service.S3Service;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class S3UseCaseTest {

    private final S3Service s3Service = mock(S3Service.class);
    private final S3UseCase s3UseCase = new S3UseCase(s3Service);

    @ParameterizedTest
    @ValueSource(strings = {"1강.pdf", "1강.PDF"})
    @DisplayName("USER_PDF는 pdf 확장자를 대소문자 구분 없이 허용하고 application/pdf로 서명한다")
    void allowPdf(String fileName) {
        s3UseCase.generatePresignedUrl(new PresignedUrlRequest(fileName, 1024L, FileType.USER_PDF));

        verify(s3Service).generatePresignedUrl(any(), eq(1024L), eq("application/pdf"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"photo.jpg", "photo.heic", "noextension", "1강.pdf.exe"})
    @DisplayName("USER_PDF에 허용되지 않은 확장자는 거절한다")
    void rejectNotSupportedExtension(String fileName) {
        PresignedUrlRequest request = new PresignedUrlRequest(fileName, 1024L, FileType.USER_PDF);

        assertThatThrownBy(() -> s3UseCase.generatePresignedUrl(request))
                .isInstanceOf(BaseException.class)
                .extracting("responseCode")
                .isEqualTo(FileResponseCode.NOT_SUPPORTED_EXTENSION);
    }
}
