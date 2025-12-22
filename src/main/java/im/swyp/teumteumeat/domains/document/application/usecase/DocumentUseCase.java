package im.swyp.teumteumeat.domains.document.application.usecase;

import im.swyp.teumteumeat.domains.document.application.dto.request.DocumentCreateRequest;
import im.swyp.teumteumeat.domains.document.application.dto.response.DocumentListResponse;
import im.swyp.teumteumeat.domains.document.application.dto.response.DocumentResponse;
import im.swyp.teumteumeat.domains.document.application.mapper.DocumentMapper;
import im.swyp.teumteumeat.domains.document.domain.service.DocumentService;
import im.swyp.teumteumeat.domains.document.domain.service.DocumentSummaryService;
import im.swyp.teumteumeat.domains.document.persistence.entity.Document;
import im.swyp.teumteumeat.domains.goal.domain.service.GoalService;
import im.swyp.teumteumeat.domains.goal.persistence.entity.Goal;
import im.swyp.teumteumeat.domains.quiz.application.usecase.QuizUseCase;
import im.swyp.teumteumeat.domains.user.domain.service.UserService;
import im.swyp.teumteumeat.domains.user.persistence.entity.UserEntity;
import im.swyp.teumteumeat.global.annotation.UseCase;
import im.swyp.teumteumeat.global.common.CommonResponseCode;
import im.swyp.teumteumeat.global.exception.BaseException;
import im.swyp.teumteumeat.infra.ocr.domain.service.OcrService;
import im.swyp.teumteumeat.infra.s3.domain.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static im.swyp.teumteumeat.domains.document.domain.constant.FileStatus.*;

@Slf4j
@UseCase
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentUseCase {

    private final DocumentService documentService;
    private final UserService userService;
    private final GoalService goalService;
    private final OcrService ocrService;
    private final DocumentSummaryService documentSummaryService;
    private final QuizUseCase quizUseCase;
    private final S3Service s3Service;

    @Transactional
    public void uploadDocument(Long userId, Long goalId, DocumentCreateRequest request) {
        UserEntity user = userService.getUserById(userId);
        Goal goal = goalService.getGoalById(goalId);

        Document document = DocumentMapper.toDocument(user, goal, request);
        documentService.createDocument(document);

        // OCR
        extractRawContent(document);

        // Summary (요약)
        documentSummaryService.generateSummary(document);

        // 퀴즈 생성
        quizUseCase.createQuizzesForPdfDocument(document, request.difficulty() != null ? request.difficulty() : 3,
                request.quizTopic());
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

    // S3에 업로드 된 문서 URL을 OCR API에 넘겨주어 텍스트를 추출하여 업데이트
    private void extractRawContent(Document document) {
        String imageUrl = s3Service.generateFileUrl(document.getFileKey());
        imageUrl = urlEncode(imageUrl);

        // OCR API 호출
        document.updateStatus(PROCESSING);

        try {
            String rawContent = ocrService.extractText(imageUrl);
            document.updateRawContent(rawContent);
        } catch (Exception e) {
            log.error("FAILED OCR PROCESSING:", e);
            document.updateStatus(FAILED);
            throw new BaseException(CommonResponseCode.INTERNAL_SERVER_ERROR);
        }

        document.updateStatus(COMPLETED);
    }

    // 외부 API의 URL 정규식 통과를 위해 한글 및 공백을 인코딩
    private String urlEncode(String imageUrl) {
        // URL에서 마지막 '/'의 위치를 찾아 경로와 파일명 분리
        int lastSlashIndex = imageUrl.lastIndexOf("/");
        String basePath = imageUrl.substring(0, lastSlashIndex + 1);
        String fileName = imageUrl.substring(lastSlashIndex + 1);

        try {
            // 파일명을 UTF-8로 인코딩 후 공백 치환 '공백' → + → %20
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                    .replace("+", "%20");

            return basePath + encodedFileName;
        } catch (Exception e) {
            log.error("URL 인코딩 중 오류 발생: imageUrl={}", imageUrl, e);
        }

        return imageUrl;
    }
}
