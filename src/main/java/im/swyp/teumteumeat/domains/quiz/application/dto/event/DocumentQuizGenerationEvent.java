package im.swyp.teumteumeat.domains.quiz.application.dto.event;

public record DocumentQuizGenerationEvent(
        Long documentId,
        Long userId
) {}
