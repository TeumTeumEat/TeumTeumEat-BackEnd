package im.swyp.teumteumeat.domains.notice.application.mapper;

import im.swyp.teumteumeat.domains.notice.application.dto.request.NoticeCreateRequest;
import im.swyp.teumteumeat.domains.notice.application.dto.response.NoticePageResponse;
import im.swyp.teumteumeat.domains.notice.application.dto.response.NoticeResponse;
import im.swyp.teumteumeat.domains.notice.application.dto.response.NoticeSliceResponse;
import im.swyp.teumteumeat.domains.notice.persistence.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;

public class NoticeMapper {
    public static Notice toNotice(NoticeCreateRequest request) {
        return Notice.builder()
                .title(request.title())
                .content(request.content())
                .build();
    }

    public static NoticeResponse fromNotice(Notice notice) {
        return NoticeResponse.builder()
                .noticeId(notice.getId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .createdDate(notice.getCreatedDate())
                .build();
    }

    public static NoticeSliceResponse toNoticeSliceResponse(Slice<Notice> slice) {
        return NoticeSliceResponse.builder()
                .notices(slice.getContent().stream().map(NoticeMapper::fromNotice).toList())
                .page(slice.getNumber())
                .size(slice.getSize())
                .hasNext(slice.hasNext())
                .build();
    }

    public static NoticePageResponse toNoticePageResponse(Page<Notice> page) {
        return NoticePageResponse.builder()
                .notices(page.getContent().stream().map(NoticeMapper::fromNotice).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }
}
