package im.swyp.teumteumeat.infra.s3.application.usecase;

import im.swyp.teumteumeat.global.annotation.UseCase;
import im.swyp.teumteumeat.global.exception.BaseException;
import im.swyp.teumteumeat.infra.s3.application.mapper.S3Mapper;
import im.swyp.teumteumeat.infra.s3.constant.FileResponseCode;
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
        validSupportedExtension(fileName);

        String key = s3Service.createKey(fileName);
        URL presignedUrl = s3Service.generatePresignedUrl(key);
//        String fileUrl = s3Service.generateFileUrl(key);

        return S3Mapper.toPresignedUrlResponse(presignedUrl, key);
    }

    // PDF 파일 확장자인지 검사
    private void validSupportedExtension(String fileName) {
        if (!Objects.equals(StringUtils.getFilenameExtension(fileName), "pdf")) {
            throw new BaseException(FileResponseCode.NOT_SUPPORTED_EXTENSION);
        }
    }
}
