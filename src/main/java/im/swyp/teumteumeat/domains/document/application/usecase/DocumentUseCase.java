package im.swyp.teumteumeat.domains.document.application.usecase;

import im.swyp.teumteumeat.domains.document.application.dto.request.DocumentCreateRequest;
import im.swyp.teumteumeat.domains.document.application.dto.request.OcrInitRequest;
import im.swyp.teumteumeat.domains.document.application.dto.request.OcrPartRequest;
import im.swyp.teumteumeat.domains.document.application.dto.response.DocumentListResponse;
import im.swyp.teumteumeat.domains.document.application.dto.response.DocumentResponse;
import im.swyp.teumteumeat.domains.document.application.mapper.DocumentMapper;
import im.swyp.teumteumeat.domains.document.application.mapper.DocumentPartMapper;
import im.swyp.teumteumeat.domains.document.domain.constant.FileStatus;
import im.swyp.teumteumeat.domains.document.domain.service.DocumentService;
import im.swyp.teumteumeat.domains.document.domain.service.DocumentSummaryService;
import im.swyp.teumteumeat.domains.document.persistence.entity.Document;
import im.swyp.teumteumeat.domains.document.persistence.entity.DocumentPart;
import im.swyp.teumteumeat.domains.goal.domain.service.GoalService;
import im.swyp.teumteumeat.domains.goal.persistence.entity.Goal;
import im.swyp.teumteumeat.domains.quiz.application.usecase.QuizUseCase;
import im.swyp.teumteumeat.domains.user.domain.service.UserService;
import im.swyp.teumteumeat.domains.user.persistence.entity.UserEntity;
import im.swyp.teumteumeat.global.annotation.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import im.swyp.teumteumeat.domains.document.application.dto.response.DocumentDetailResponse;
import im.swyp.teumteumeat.domains.userQuiz.domain.service.UserQuizService;
import im.swyp.teumteumeat.global.exception.BaseException;
import im.swyp.teumteumeat.domains.goal.domain.constant.GoalResponseCode;
import im.swyp.teumteumeat.domains.quiz.domain.constant.QuizResponseCode;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@UseCase
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentUseCase {

    private final DocumentService documentService;
    private final UserService userService;
    private final GoalService goalService;
    private final DocumentSummaryService documentSummaryService;
    private final QuizUseCase quizUseCase;
    private final UserQuizService userQuizService;

    @Transactional
    public void uploadDocument(Long userId, Long goalId, DocumentCreateRequest request) {
        UserEntity user = userService.getUserById(userId);
        Goal goal = goalService.getGoalById(goalId);

        Document document = DocumentMapper.toDocument(user, goal, request);
        documentService.createDocument(document);
    }

    // fileKey로 문서 parts 설정
    @Transactional
    public void setParts(OcrInitRequest request) {
        Document document = documentService.getDocumentByFileKey(request.fileKey());

        // OCR 처리가 필요한 경우
        if (request.needOcr()) {
            document.updateTotalParts(request.totalParts());
            document.updateStatus(FileStatus.PROCESSING);
        }
        // PDF에서 텍스트 추출이 완료된 경우
        else {
            document.updateRawContent(request.rawContent());
            document.updateStatus(FileStatus.COMPLETED);
        }
    }

    @Transactional
    public void saveParts(OcrPartRequest request) {
        Document document = documentService.getDocumentByFileKey(request.fileKey());

        DocumentPart documentPart = DocumentPartMapper.toDocumentPart(
                document,
                request.partIndex(),
                request.ocrText());
        documentService.createDocumentPart(documentPart);

        if (document.isAllPartsCollected()) {
            List<DocumentPart> parts = document.getParts();

            String combinedText = parts.stream()
                    .sorted(Comparator.comparing(DocumentPart::getPartIndex))
                    .map(DocumentPart::getOcrText)
                    .collect(Collectors.joining(" "));
            document.updateRawContent(combinedText);

            // Summary (요약)
            documentSummaryService.generateSummary(document);

            // 퀴즈 생성
            quizUseCase.createQuizzesForPdfDocument(document);

            document.updateStatus(FileStatus.COMPLETED);
            document.getParts().clear();
        }
    }

    // 해당 목표의 모든 문서 반환
    public DocumentListResponse getDocuments(Long userId, Long goalId) {
        Goal goal = goalService.getGoalById(goalId);
        goal.validateOwner(userId);

        List<Document> documents = documentService.getDocumentsByGoalId(goalId);
        List<DocumentResponse> responses = documents.stream().map(DocumentMapper::fromDocument).toList();
        return DocumentMapper.toDocumentListResponse(responses);
    }

    // 특정 문서 반환 (단순 조회)
    public DocumentResponse getDocument(Long userId, Long goalId, Long documentId) {
        Goal goal = goalService.getGoalById(goalId);
        goal.validateOwner(userId);

        Document document = documentService.getDocumentById(documentId);
        document.validateOwner(userId);

        return DocumentMapper.fromDocument(document);
    }

    // 문서 요약 및 상세 조회 (단순 조회 - 퀴즈 풀기 전)
    @Transactional(readOnly = true)
    public DocumentDetailResponse getSummaryForView(Long userId, Long goalId, Long documentId) {
        Goal goal = goalService.getGoalById(goalId);
        goal.validateOwner(userId);

        // 1. Goal 만료 확인
        if (goal.getEndDate().isBefore(LocalDate.now())) {
            throw new BaseException(GoalResponseCode.GOAL_EXPIRED);
        }

        boolean hasSolvedToday = userQuizService.hasSolvedQuizTodayByGoal(userId, goalId);
        boolean isFirstTime = !userQuizService.hasSolvedAnyQuizEver(userId);

        Document document = documentService.getDocumentById(documentId);
        document.validateOwner(userId);

        return DocumentMapper.toDocumentDetailResponse(document, hasSolvedToday, isFirstTime);
    }

    // 문서 요약 및 상세 조회 (학습 시 사용 되는 메서드)
    @Transactional
    public DocumentDetailResponse createSummary(Long userId, Long goalId, Long documentId) {
        Goal goal = goalService.getGoalById(goalId);
        goal.validateOwner(userId);

        // 1. Goal 만료 확인
        if (goal.getEndDate().isBefore(LocalDate.now())) {
            throw new BaseException(GoalResponseCode.GOAL_EXPIRED);
        }

        // 2. 요약글 생성 1회 제한 확인 (오늘 이미 학습했는지)
        if (userQuizService.hasSolvedQuizTodayByGoal(userId, goalId)) {
            throw new BaseException(QuizResponseCode.TODAY_QUOTA_EXCEEDED);
        }

        Document document = documentService.getDocumentById(documentId);
        document.validateOwner(userId);

        // 3. 학습하지 않았을 시 새로운 요약글 및 퀴즈 생성
        documentSummaryService.generateSummary(document);
        quizUseCase.createQuizzesForPdfDocument(document);

        boolean isFirstTime = !userQuizService.hasSolvedAnyQuizEver(userId);

        return DocumentMapper.toDocumentDetailResponse(document, false, isFirstTime);
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
