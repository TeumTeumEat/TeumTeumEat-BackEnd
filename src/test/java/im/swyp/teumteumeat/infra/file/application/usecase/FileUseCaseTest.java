package im.swyp.teumteumeat.infra.file.application.usecase;

import im.swyp.teumteumeat.global.exception.BaseException;
import im.swyp.teumteumeat.infra.file.application.dto.PresignedUrlRequest;
import im.swyp.teumteumeat.infra.file.application.dto.PresignedUrlResponse;
import im.swyp.teumteumeat.infra.file.constant.FileResponseCode;
import im.swyp.teumteumeat.infra.file.domain.service.FileStorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.MalformedURLException;
import java.net.URL;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class FileUseCaseTest {

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private FileUseCase fileUseCase;

    @Test
    @DisplayName("올바른 PDF 파일명이 들어오면 Presigned Url을 생성한다.")
    void generatePresignedUrl_Success() throws MalformedURLException {
        //given
        String fileName = "시스템구조_1강.pdf";
        String mockKey = "1234abc/uuid_시스템구조_1강.pdf";
        URL mockUrl = new URL("https://s3.amazon.com/test");
        PresignedUrlRequest request = new PresignedUrlRequest(fileName);

        given(fileStorageService.generateFileKey(fileName)).willReturn(mockKey);
        given(fileStorageService.getUploadUrl(mockKey)).willReturn(mockUrl);

        // when
        PresignedUrlResponse response = fileUseCase.generatePresignedUrl(request);

        // then
        assertThat(response.presignedUrl()).isEqualTo(mockUrl);
        assertThat(response.key()).isEqualTo(mockKey);
    }

    @Test
    @DisplayName("확장자가 PDF가 아닌 경우 예외가 발생한다")
    void generatePresignedUrl_Fail() {
        // given
        PresignedUrlRequest request = new PresignedUrlRequest("hacker.exe");

        // when & then
        BaseException exception = assertThrows(BaseException.class, () -> fileUseCase.generatePresignedUrl(request));

        assertEquals(FileResponseCode.NOT_SUPPORTED_EXTENSION, exception.getResponseCode());
    }
}