package im.swyp.teumteumeat.domains.document.application.usecase;

import im.swyp.teumteumeat.domains.document.application.dto.request.DocumentCreateRequest;
import im.swyp.teumteumeat.domains.document.application.dto.response.DocumentListResponse;
import im.swyp.teumteumeat.domains.document.application.dto.response.DocumentResponse;
import im.swyp.teumteumeat.domains.document.application.mapper.DocumentMapper;
import im.swyp.teumteumeat.domains.document.domain.service.DocumentService;
import im.swyp.teumteumeat.domains.document.persistence.entity.Document;
import im.swyp.teumteumeat.domains.goal.domain.service.GoalService;
import im.swyp.teumteumeat.domains.goal.persistence.entity.Goal;
import im.swyp.teumteumeat.domains.user.domain.service.UserService;
import im.swyp.teumteumeat.domains.user.persistence.entity.UserEntity;
import im.swyp.teumteumeat.global.annotation.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import im.swyp.teumteumeat.domains.document.domain.service.OCRService;
import im.swyp.teumteumeat.domains.quiz.application.usecase.QuizUseCase;

@UseCase
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentUseCase {

    private final DocumentService documentService;
    private final UserService userService;
    private final GoalService goalService;
    private final QuizUseCase quizUseCase;
    private final OCRService ocrService;

    @Transactional
    public void uploadDocument(Long userId, Long goalId, DocumentCreateRequest request) {
        UserEntity user = userService.getUserById(userId);
        Goal goal = goalService.getGoalById(goalId);

        Document document = DocumentMapper.toDocument(user, goal, request);
        documentService.createDocument(document);

        // OCR
        ocrService.extractContent(document);
        // 퀴즈 생성
        quizUseCase.createQuizzesForPdfDocument(document.getId());
    }

    // 해당 목표의 모든 문서 반환
    public DocumentListResponse getDocuments(Long userId, Long goalId) {
        Goal goal = goalService.getGoalById(goalId);
        goal.validateOwner(userId);

        List<Document> documents = documentService.getDocumentsByGoalId(goalId);
        List<DocumentResponse> responses = documents.stream().map(DocumentMapper::fromDocument).toList();
        return DocumentMapper.toDocumentListResponse(responses);
    }

    // 특정 문서 반환
    public DocumentResponse getDocument(Long userId, Long goalId, Long documentId) {
        Goal goal = goalService.getGoalById(goalId);
        goal.validateOwner(userId);

        Document document = documentService.getDocumentById(documentId);
        document.validateOwner(userId);

        return DocumentMapper.fromDocument(document);
    }

    // 해당 목표의 모든 문서 삭제
    @Transactional
    public void deleteDocuments(Long userId, Long goalId) {
        Goal goal = goalService.getGoalById(goalId);
        goal.validateOwner(userId);

        documentService.deleteDocumentsByGoalId(goalId);
    }

    // 특정 문서 삭제
    @Transactional
    public void deleteDocument(Long userId, Long goalId, Long documentId) {
        Goal goal = goalService.getGoalById(goalId);
        goal.validateOwner(userId);

        Document document = documentService.getDocumentById(documentId);
        document.validateOwner(userId);

        documentService.deleteDocument(documentId);
    }
}
