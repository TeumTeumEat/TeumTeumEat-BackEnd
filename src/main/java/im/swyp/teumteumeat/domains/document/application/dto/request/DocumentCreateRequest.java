package im.swyp.teumteumeat.domains.document.application.dto.request;

public record DocumentCreateRequest(

        String fileName,
        String fileKey
) {
}
