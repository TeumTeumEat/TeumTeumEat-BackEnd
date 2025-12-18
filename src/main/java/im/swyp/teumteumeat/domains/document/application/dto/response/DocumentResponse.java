package im.swyp.teumteumeat.domains.document.application.dto.response;

import im.swyp.teumteumeat.domains.document.domain.constant.FileStatus;
import lombok.Builder;

@Builder
public record DocumentResponse(

        Long documentId,
        String fileName,
        String fileKey,
        FileStatus status
) {
}
