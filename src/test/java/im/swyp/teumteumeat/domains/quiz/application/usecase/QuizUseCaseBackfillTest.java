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
import im.swyp.teumteumeat.domains.llm.domain.constant.LLMResponseCode;
import im.swyp.teumteumeat.domains.llm.domain.service.LLMService;
import im.swyp.teumteumeat.domains.quiz.application.mapper.QuizMapper;
import im.swyp.teumteumeat.domains.quiz.domain.constant.QuizType;
import im.swyp.teumteumeat.domains.quiz.domain.service.QuizService;
import im.swyp.teumteumeat.domains.quiz.persistence.entity.Quiz;
import im.swyp.teumteumeat.domains.user.domain.service.UserService;
import im.swyp.teumteumeat.domains.user.persistence.entity.UserEntity;
import im.swyp.teumteumeat.global.component.DistributedLockFacade;
import im.swyp.teumteumeat.global.exception.BaseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
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

// executeQuizGeneration의 검증 관련 동작을 확인한다.
// - 검증으로 일부 퀴즈가 걸러졌을 때 부족분을 재시도로 채우는지
// - 검증 콜 자체가 실패(429 등)해도 이미 생성된 퀴즈가 버려지지 않고 저장되는지
// 실제 LLM 호출 없이 LLMService만 목킹해 로직 자체의 동작을 검증한다.
class QuizUseCaseBackfillTest {

    private QuizService quizService;
    private LLMService llmService;
    private SimpleMeterRegistry meterRegistry;
    private QuizUseCase quizUseCase;

    @BeforeEach
    void setUp() {
        quizService = mock(QuizService.class);
        llmService = mock(LLMService.class);
        meterRegistry = new SimpleMeterRegistry();
        DistributedLockFacade distributedLockFacade = mock(DistributedLockFacade.class);
        CategoryDocumentService categoryDocumentService = mock(CategoryDocumentService.class);
        QuizMapper quizMapper = mock(QuizMapper.class);
        DocumentService documentService = mock(DocumentService.class);
        DocumentSectionService documentSectionService = mock(DocumentSectionService.class);
        UserService userService = mock(UserService.class);
        GoalService goalService = mock(GoalService.class);
        DocumentSummaryService documentSummaryService = mock(DocumentSummaryService.class);

        quizUseCase = new QuizUseCase(
                quizService, meterRegistry, distributedLockFacade, categoryDocumentService, llmService,
                quizMapper, new ObjectMapper(), documentService, documentSectionService, userService, goalService,
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
    }

    @Test
    void 검증으로_일부_무효_판정되면_부족분을_재시도로_채운다() {
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
        assertThat(savedQuizzes()).hasSize(5);
    }

    @Test
    void 검증_콜이_실패해도_생성된_퀴즈는_검증_없이_저장된다() {
        when(llmService.generateAnswer(anyString()))
                .thenReturn(new LLMResponse(List.of(
                        sampleQuiz(), sampleQuiz(), sampleQuiz(), sampleQuiz(), sampleQuiz())));
        when(llmService.validateQuizzes(anyString()))
                .thenThrow(new BaseException(LLMResponseCode.AI_QUOTA_EXCEEDED));

        quizUseCase.createQuizzesForDocument(1L, 1L, 5);

        // 검증을 건너뛴 걸로 간주해 목표를 채운 것으로 처리 -> 불필요한 재시도 라운드가 돌지 않아야 함
        verify(llmService, times(1)).generateAnswer(anyString());
        assertThat(savedQuizzes()).hasSize(5);
        assertThat(meterRegistry.counter("quiz.validation.skipped").count()).isEqualTo(5.0);
    }

    @Test
    void 뒤_라운드에서_검증_콜이_실패해도_앞_라운드에서_확보한_퀴즈와_함께_저장된다() {
        // 1차: 5문제 중 index 2 무효 -> 4개 확보, 2차: 부족분 1개 생성 후 검증 콜 실패 -> 검증 없이 포함
        when(llmService.generateAnswer(anyString()))
                .thenReturn(new LLMResponse(List.of(
                        sampleQuiz(), sampleQuiz(), sampleQuiz(), sampleQuiz(), sampleQuiz())))
                .thenReturn(new LLMResponse(List.of(sampleQuiz())));
        when(llmService.validateQuizzes(anyString()))
                .thenReturn(new QuizValidationResponse(List.of(
                        new QuizValidationResponse.Result(0, true),
                        new QuizValidationResponse.Result(1, true),
                        new QuizValidationResponse.Result(2, false),
                        new QuizValidationResponse.Result(3, true),
                        new QuizValidationResponse.Result(4, true))))
                .thenThrow(new BaseException(LLMResponseCode.AI_SERVER_ERROR));

        quizUseCase.createQuizzesForDocument(1L, 1L, 5);

        verify(llmService, times(2)).generateAnswer(anyString());
        assertThat(savedQuizzes()).hasSize(5);
        assertThat(meterRegistry.counter("quiz.validation.skipped").count()).isEqualTo(1.0);
    }

    private List<Quiz> savedQuizzes() {
        ArgumentCaptor<List<Quiz>> savedQuizzesCaptor = ArgumentCaptor.forClass(List.class);
        verify(quizService).saveQuizzes(savedQuizzesCaptor.capture());
        return savedQuizzesCaptor.getValue();
    }

    private LLMResponse.Quiz sampleQuiz() {
        return new LLMResponse.Quiz("문제 내용", List.of("O", "X"), "O", QuizType.OX, "해설 내용");
    }
}
