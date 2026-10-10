package im.swyp.teumteumeat.infra.s3.application.usecase;

import im.swyp.teumteumeat.global.annotation.UseCase;
import im.swyp.teumteumeat.global.exception.BaseException;
import im.swyp.teumteumeat.infra.s3.application.mapper.S3Mapper;
import im.swyp.teumteumeat.infra.s3.constant.FileResponseCode;
import im.swyp.teumteumeat.infra.s3.constant.FileType;
import im.swyp.teumteumeat.infra.s3.domain.service.S3Service;
import im.swyp.teumteumeat.infra.s3.application.dto.PresignedUrlRequest;
import im.swyp.teumteumeat.infra.s3.application.dto.PresignedUrlResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

import java.net.URL;
import java.util.Objects;

@UseCase
@RequiredArgsConstructor
public class S3UseCase {

    private final S3Service s3Service;

    public PresignedUrlResponse generatePresignedUrl(
            PresignedUrlRequest request
    ) {
        String fileName = request.fileName();
        Long contentLength = request.fileSize();
        FileType fileType = request.fileType();
        validSupportedExtension(fileName, fileType);
        validFileSizeLimit(contentLength, fileType);

        String key = s3Service.createKey(fileName, fileType);
        URL presignedUrl = s3Service.generatePresignedUrl(key, contentLength, fileType);
//        String fileUrl = s3Service.generateFileUrl(key);

        return S3Mapper.toPresignedUrlResponse(presignedUrl, key);
    }

    // 파일 종류에 맞는 확장자인지 검사
    private void validSupportedExtension(String fileName, FileType fileType) {
        if (!Objects.equals(StringUtils.getFilenameExtension(fileName), fileType.getExtension())) {
            throw new BaseException(FileResponseCode.NOT_SUPPORTED_EXTENSION);
        }
    }

    private void validFileSizeLimit(Long contentLength, FileType fileType) {
        if (contentLength != null && contentLength > fileType.getMaxSize()) {
            throw new BaseException(FileResponseCode.EXCEED_FILE_SIZE);
        }
    }
}
