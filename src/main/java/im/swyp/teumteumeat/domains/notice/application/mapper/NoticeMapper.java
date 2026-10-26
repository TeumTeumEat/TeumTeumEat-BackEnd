package im.swyp.teumteumeat.domains.notice.application.mapper;

import im.swyp.teumteumeat.domains.notice.application.dto.request.NoticeContentRequest;
import im.swyp.teumteumeat.domains.notice.application.dto.response.NoticeAdminResponse;
import im.swyp.teumteumeat.domains.notice.application.dto.response.NoticeDetailResponse;
import im.swyp.teumteumeat.domains.notice.application.dto.response.NoticeModalListResponse;
import im.swyp.teumteumeat.domains.notice.application.dto.response.NoticeModalResponse;
import im.swyp.teumteumeat.domains.notice.application.dto.response.NoticePageResponse;
import im.swyp.teumteumeat.domains.notice.application.dto.response.NoticeResponse;
import im.swyp.teumteumeat.domains.notice.application.dto.response.NoticeSliceResponse;
import im.swyp.teumteumeat.domains.notice.persistence.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.util.StringUtils;

import java.util.List;

public class NoticeMapper {
    public static Notice toNotice(NoticeContentRequest request) {
        // 빈 문자열 링크는 링크 없음(null)으로 저장, 링크가 없으면 버튼 문구도 저장하지 않음
        return Notice.builder()
                .title(request.title())
                .fullScreenImageKey(request.fullScreenImageKey())
                .modalImageKey(request.modalImageKey())
                .externalLink(StringUtils.hasText(request.externalLink()) ? request.externalLink() : null)
                .deepLink(StringUtils.hasText(request.deepLink()) ? request.deepLink() : null)
                .actionButtonText(request.hasActionLink() ? request.actionButtonText() : null)
                .popupStartAt(request.popupStartAt())
                .popupEndAt(request.popupEndAt())
                .build();
    }

    public static NoticeResponse toNoticeResponse(Notice notice, NoticeDetailResponse detail) {
        return NoticeResponse.builder()
                .noticeId(notice.getId())
                .title(notice.getTitle())
                .createdDate(notice.getCreatedDate())
                .detail(detail)
                .build();
    }

    public static NoticeDetailResponse toNoticeDetailResponse(Notice notice, String fullScreenImageUrl) {
        return NoticeDetailResponse.builder()
                .fullScreenImageUrl(fullScreenImageUrl)
                .externalLink(notice.getExternalLink())
                .deepLink(notice.getDeepLink())
                .actionButtonText(notice.getActionButtonText())
                .build();
    }

    public static NoticeModalResponse toNoticeModalResponse(Notice notice, String modalImageUrl, NoticeDetailResponse detail) {
        return NoticeModalResponse.builder()
                .noticeId(notice.getId())
                .title(notice.getTitle())
                .modalImageUrl(modalImageUrl)
                .detail(detail)
                .build();
    }

    public static NoticeModalListResponse toNoticeModalListResponse(List<NoticeModalResponse> notices) {
        return NoticeModalListResponse.builder()
                .notices(notices)
                .build();
    }

    public static NoticeAdminResponse toNoticeAdminResponse(Notice notice, String fullScreenImageUrl, String modalImageUrl) {
        return NoticeAdminResponse.builder()
                .noticeId(notice.getId())
                .title(notice.getTitle())
                .fullScreenImageKey(notice.getFullScreenImageKey())
                .fullScreenImageUrl(fullScreenImageUrl)
                .modalImageKey(notice.getModalImageKey())
                .modalImageUrl(modalImageUrl)
                .externalLink(notice.getExternalLink())
                .deepLink(notice.getDeepLink())
                .actionButtonText(notice.getActionButtonText())
                .popupStartAt(notice.getPopupStartAt())
                .popupEndAt(notice.getPopupEndAt())
                .createdDate(notice.getCreatedDate())
                .build();
    }

    public static NoticeSliceResponse toNoticeSliceResponse(Slice<NoticeResponse> slice) {
        return NoticeSliceResponse.builder()
                .notices(slice.getContent())
                .page(slice.getNumber())
                .size(slice.getSize())
                .hasNext(slice.hasNext())
                .build();
    }

    public static NoticePageResponse toNoticePageResponse(Page<NoticeAdminResponse> page) {
        return NoticePageResponse.builder()
                .notices(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }
}
