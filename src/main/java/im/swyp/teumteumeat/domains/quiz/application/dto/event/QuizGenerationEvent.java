package im.swyp.teumteumeat.domains.quiz.application.dto.event;

public record QuizGenerationEvent(
        Long documentId,
        Long userId
) {}
