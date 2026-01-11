package im.swyp.teumteumeat.domains.history.application.dto.response;

import im.swyp.teumteumeat.domains.goal.domain.constant.GoalType;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record DailyHistoryResponse(
                Long id, // DocumentId 또는 CategoryDocumentId
                GoalType type, // DOCUMENT, CATEGORY
                String title, // Category명 또는 Document 파일명
                String summarySnippet, // 1~2 문장
                LocalDateTime lastStudiedAt) {
}
