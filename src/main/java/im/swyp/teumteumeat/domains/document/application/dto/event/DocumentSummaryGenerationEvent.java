package im.swyp.teumteumeat.domains.document.application.dto.event;

import im.swyp.teumteumeat.domains.document.persistence.entity.Document;
import im.swyp.teumteumeat.domains.goal.persistence.entity.Goal;

public record DocumentSummaryGenerationEvent(
        Long userId,
        Goal goal,
        Document document,
        boolean isFirstTime
) {}
