package im.swyp.teumteumeat.domains.document.application.usecase;

import im.swyp.teumteumeat.domains.document.application.dto.response.DocumentDetailResponse;
import im.swyp.teumteumeat.domains.document.application.mapper.DocumentMapper;
import im.swyp.teumteumeat.domains.document.domain.service.DocumentService;
import im.swyp.teumteumeat.domains.document.domain.service.DocumentSummaryService;
import im.swyp.teumteumeat.domains.document.persistence.entity.Document;
import im.swyp.teumteumeat.domains.document.persistence.entity.DocumentSummary;
import im.swyp.teumteumeat.domains.document.persistence.repository.DocumentSummaryRepository;
import im.swyp.teumteumeat.domains.goal.domain.constant.GoalResponseCode;
import im.swyp.teumteumeat.domains.goal.domain.service.GoalService;
import im.swyp.teumteumeat.domains.goal.persistence.entity.Goal;
import im.swyp.teumteumeat.domains.quiz.application.usecase.QuizUseCase;
import im.swyp.teumteumeat.domains.quiz.domain.constant.QuizResponseCode;
import im.swyp.teumteumeat.domains.userQuiz.domain.service.UserQuizService;
import im.swyp.teumteumeat.global.annotation.UseCase;
import im.swyp.teumteumeat.global.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@UseCase
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentSummaryUseCase {

    private final GoalService goalService;
    private final UserQuizService userQuizService;
    private final DocumentService documentService;
    private final DocumentSummaryRepository documentSummaryRepository;
    private final DocumentSummaryService documentSummaryService;
    private final QuizUseCase quizUseCase;

    // 문서 요약 및 상세 조회 (단순 조회 - 퀴즈 풀기 전)
    @Transactional
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

        // 오늘 퀴즈를 풀지 않았는데, 문서가 오늘 업데이트된 것이 아니라면 -> 요약글 재생성 (Smart GET)
        Optional<DocumentSummary> latestSummaryOpt = documentSummaryRepository.findLatestByDocumentId(documentId);
        boolean isCreatedToday = latestSummaryOpt.map(s -> s.getCreatedDate().toLocalDate().isEqual(LocalDate.now()))
                .orElse(false);

        DocumentSummary summary;
        if (!hasSolvedToday && !isCreatedToday) {
            summary = documentSummaryService.generateSummary(document);
            quizUseCase.createQuizzesForPdfDocument(document, summary);
        } else {
            summary = latestSummaryOpt.orElseGet(() -> documentSummaryService.generateSummary(document));
        }

        return DocumentMapper.toDocumentDetailResponse(document, summary, hasSolvedToday, isFirstTime);
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
        DocumentSummary summary = documentSummaryService.generateSummary(document);
        quizUseCase.createQuizzesForPdfDocument(document, summary);

        boolean isFirstTime = !userQuizService.hasSolvedAnyQuizEver(userId);

        return DocumentMapper.toDocumentDetailResponse(document, summary, false, isFirstTime);
    }
}
