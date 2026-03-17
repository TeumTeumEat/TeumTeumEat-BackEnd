package im.swyp.teumteumeat.domains.userQuiz.application.dto.event;

public record UserQuizGenerationEvent(
        Long documentId,
        Long userId,
        int quizCount
) {}
