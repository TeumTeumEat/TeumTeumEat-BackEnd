package im.swyp.teumteumeat.domains.quiz.application.usecase;

import im.swyp.teumteumeat.domains.categoryDocument.domain.service.CategoryDocumentService;
import im.swyp.teumteumeat.domains.categoryDocument.persistence.entity.CategoryDocument;
import im.swyp.teumteumeat.domains.category.persistence.entity.Category;
import im.swyp.teumteumeat.domains.document.domain.service.DocumentSectionService;
import im.swyp.teumteumeat.domains.document.domain.service.DocumentService;
import im.swyp.teumteumeat.domains.document.domain.service.DocumentSummaryService;
import im.swyp.teumteumeat.domains.goal.domain.constant.Difficulty;
import im.swyp.teumteumeat.domains.goal.domain.service.GoalService;
import im.swyp.teumteumeat.domains.goal.persistence.entity.Goal;
import im.swyp.teumteumeat.domains.llm.application.dto.response.LLMResponse;
import im.swyp.teumteumeat.domains.llm.application.dto.response.QuizValidationResponse;
import im.swyp.teumteumeat.domains.llm.domain.service.LLMService;
import im.swyp.teumteumeat.domains.quiz.application.mapper.QuizMapper;
import im.swyp.teumteumeat.domains.quiz.domain.constant.QuizType;
import im.swyp.teumteumeat.domains.quiz.domain.service.QuizService;
import im.swyp.teumteumeat.domains.quiz.persistence.entity.Quiz;
import im.swyp.teumteumeat.domains.user.domain.service.UserService;
import im.swyp.teumteumeat.domains.user.persistence.entity.UserEntity;
import im.swyp.teumteumeat.global.component.DistributedLockFacade;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// 검증(사전 검증)으로 일부 퀴즈가 걸러졌을 때, executeQuizGeneration이 부족분을 재시도로 채우는지 확인.
// 실제 LLM 호출 없이 LLMService만 목킹해 재시도 루프 자체의 동작을 검증한다.
class QuizUseCaseBackfillTest {

    @Test
    void 검증으로_일부_무효_판정되면_부족분을_재시도로_채운다() {
        QuizService quizService = mock(QuizService.class);
        DistributedLockFacade distributedLockFacade = mock(DistributedLockFacade.class);
        CategoryDocumentService categoryDocumentService = mock(CategoryDocumentService.class);
        LLMService llmService = mock(LLMService.class);
        QuizMapper quizMapper = mock(QuizMapper.class);
        DocumentService documentService = mock(DocumentService.class);
        DocumentSectionService documentSectionService = mock(DocumentSectionService.class);
        UserService userService = mock(UserService.class);
        GoalService goalService = mock(GoalService.class);
        DocumentSummaryService documentSummaryService = mock(DocumentSummaryService.class);

        QuizUseCase quizUseCase = new QuizUseCase(
                quizService, distributedLockFacade, categoryDocumentService, llmService, quizMapper,
                new ObjectMapper(), documentService, documentSectionService, userService, goalService,
                documentSummaryService);

        Category category = Category.builder().name("테스트").path("test/path").description("설명").build();
        CategoryDocument document = CategoryDocument.builder().content("본문").category(category).build();
        Goal goal = Goal.builder().difficulty(Difficulty.EASY).category(category).build();
        UserEntity user = mock(UserEntity.class);

        when(categoryDocumentService.getDocumentWithCategoryById(any())).thenReturn(document);
        when(goalService.findLatestGoal(any(), any())).thenReturn(goal);
        when(goalService.getTopic(any(), any())).thenReturn("전반적인 내용");
        when(userService.getUserById(any())).thenReturn(user);
        when(user.canSolveDailyQuiz()).thenReturn(true);
        when(quizService.buildQuizFromCategoryDocument(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(mock(Quiz.class));

        // 1차 생성: 5문제 중 index 2가 무효 판정 -> 4개만 확보
        when(llmService.generateAnswer(anyString()))
                .thenReturn(new LLMResponse(List.of(
                        sampleQuiz(), sampleQuiz(), sampleQuiz(), sampleQuiz(), sampleQuiz())))
                // 2차 생성(부족분 1개 요청): 1개 확보
                .thenReturn(new LLMResponse(List.of(sampleQuiz())));

        when(llmService.validateQuizzes(anyString()))
                .thenReturn(new QuizValidationResponse(List.of(
                        new QuizValidationResponse.Result(0, true),
                        new QuizValidationResponse.Result(1, true),
                        new QuizValidationResponse.Result(2, false),
                        new QuizValidationResponse.Result(3, true),
                        new QuizValidationResponse.Result(4, true))))
                .thenReturn(new QuizValidationResponse(List.of(
                        new QuizValidationResponse.Result(0, true))));

        quizUseCase.createQuizzesForDocument(1L, 1L, 5);

        // 부족분 재시도로 generateAnswer/validateQuizzes가 각각 2번씩 호출됐는지 확인
        verify(llmService, times(2)).generateAnswer(anyString());
        verify(llmService, times(2)).validateQuizzes(anyString());

        // 최종적으로 목표한 5개가 저장됐는지 확인
        ArgumentCaptor<List<Quiz>> savedQuizzesCaptor = ArgumentCaptor.forClass(List.class);
        verify(quizService).saveQuizzes(savedQuizzesCaptor.capture());
        assertThat(savedQuizzesCaptor.getValue()).hasSize(5);
    }

    private LLMResponse.Quiz sampleQuiz() {
        return new LLMResponse.Quiz("문제 내용", List.of("O", "X"), "O", QuizType.OX, "해설 내용");
    }
}
