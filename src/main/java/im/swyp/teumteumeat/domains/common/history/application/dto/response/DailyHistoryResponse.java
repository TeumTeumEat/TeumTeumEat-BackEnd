package im.swyp.teumteumeat.domains.common.history.application.dto.response;

import im.swyp.teumteumeat.domains.goal.domain.constant.GoalType;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record DailyHistoryResponse(
                Long id, // DocumentId 또는 CategoryDocumentId
                GoalType type, // DOCUMENT, CATEGORY
                Long goalId, // 해당 문서의 상위 목표 ID
                String title, // Category명 또는 Document 파일명
                String summarySnippet, // 1~2 문장
                Boolean isCompleted, // 목표 달성 여부
                LocalDateTime lastStudiedAt) {
}
