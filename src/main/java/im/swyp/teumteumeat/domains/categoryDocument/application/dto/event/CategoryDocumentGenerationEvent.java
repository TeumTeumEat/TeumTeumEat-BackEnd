package im.swyp.teumteumeat.domains.categoryDocument.application.dto.event;

import im.swyp.teumteumeat.domains.goal.persistence.entity.Goal;

public record CategoryDocumentGenerationEvent(
        Long categoryId,
        Long userId,
        Goal goal,
        boolean isFirstTime
) {}
