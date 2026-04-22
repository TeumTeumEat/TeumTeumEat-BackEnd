package im.swyp.teumteumeat.domains.common.file.application.usecase;

import im.swyp.teumteumeat.domains.common.file.application.dto.PresignedUrlRequest;
import im.swyp.teumteumeat.domains.common.file.application.dto.PresignedUrlResponse;
import im.swyp.teumteumeat.domains.common.file.application.mapper.FileMapper;
import im.swyp.teumteumeat.domains.common.file.constant.FileResponseCode;
import im.swyp.teumteumeat.domains.common.file.domain.service.FileStorageService;
import im.swyp.teumteumeat.global.annotation.UseCase;
import im.swyp.teumteumeat.global.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

import java.net.URL;
import java.util.Objects;

@UseCase
@RequiredArgsConstructor
public class FileUseCase {

    private final FileStorageService fileStorageService;

    public PresignedUrlResponse generatePresignedUrl(
            PresignedUrlRequest request
    ) {
        String fileName = request.fileName();
        validSupportedExtension(fileName);

        String key = fileStorageService.generateFileKey(fileName);
        URL presignedUrl = fileStorageService.getUploadUrl(key);

        return FileMapper.toPresignedUrlResponse(presignedUrl, key);
    }

    // PDF 파일 확장자인지 검사
    private void validSupportedExtension(String fileName) {
        if (!Objects.equals(StringUtils.getFilenameExtension(fileName), "pdf")) {
            throw new BaseException(FileResponseCode.NOT_SUPPORTED_EXTENSION);
        }
    }
}
