package im.swyp.teumteumeat.domains.quiz.application.usecase;

import im.swyp.teumteumeat.domains.categoryDocument.domain.service.CategoryDocumentService;
import im.swyp.teumteumeat.domains.goal.domain.constant.Difficulty;
import im.swyp.teumteumeat.domains.categoryDocument.persistence.entity.CategoryDocument;
import im.swyp.teumteumeat.domains.document.domain.service.DocumentService;
import im.swyp.teumteumeat.domains.document.persistence.entity.Document;
import im.swyp.teumteumeat.domains.document.persistence.entity.DocumentSummary;
import im.swyp.teumteumeat.domains.document.persistence.repository.DocumentSummaryRepository;
import im.swyp.teumteumeat.domains.goal.persistence.entity.Goal;

import im.swyp.teumteumeat.domains.llm.application.dto.response.LLMResponse;
import im.swyp.teumteumeat.domains.llm.domain.prompt.QuizPrompt;
import im.swyp.teumteumeat.domains.llm.domain.service.LLMService;
import im.swyp.teumteumeat.domains.quiz.application.dto.response.QuizListResponse;
import im.swyp.teumteumeat.domains.quiz.application.mapper.QuizMapper;
import im.swyp.teumteumeat.domains.quiz.domain.service.QuizService;
import im.swyp.teumteumeat.domains.quiz.persistence.entity.Quiz;
import im.swyp.teumteumeat.domains.user.domain.service.UserService;
import im.swyp.teumteumeat.domains.user.persistence.entity.UserEntity;
import im.swyp.teumteumeat.domains.user.domain.constant.Role;
import im.swyp.teumteumeat.global.annotation.UseCase;
import im.swyp.teumteumeat.domains.goal.domain.service.GoalService;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import java.util.function.BiConsumer;

import im.swyp.teumteumeat.global.exception.BaseException;
import im.swyp.teumteumeat.domains.goal.domain.constant.GoalResponseCode;
import im.swyp.teumteumeat.domains.quiz.domain.constant.QuizResponseCode;
import im.swyp.teumteumeat.domains.quiz.domain.constant.QuizType;

import java.time.LocalDate;
import java.util.List;

@UseCase
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuizUseCase {

    private final QuizService quizService;
    private final CategoryDocumentService categoryDocumentService;
    private final LLMService llmService;
    private final QuizMapper quizMapper;
    private final ObjectMapper objectMapper;
    private final DocumentService documentService;
    private final UserService userService;
    private final GoalService goalService;
    private final DocumentSummaryRepository documentSummaryRepository;

    // 카테고리 기반 퀴즈
    public QuizListResponse getQuizzesByCategoryDocumentId(Long categoryDocumentId) {
        List<Quiz> quizzes = quizService.getQuizzesByCategoryDocumentId(categoryDocumentId);
        List<QuizListResponse.QuizDto> quizDtos = quizzes.stream()
                .map(quizMapper::toDto)
                .toList();
        return new QuizListResponse(quizDtos);
    }

    // pdf 자료 기반 퀴즈
    public QuizListResponse getQuizzesByDocumentId(Long documentId) {
        List<Quiz> quizzes = quizService.getQuizzesByDocumentId(documentId);
        List<QuizListResponse.QuizDto> quizDtos = quizzes.stream()
                .map(quizMapper::toDto)
                .toList();
        return new QuizListResponse(quizDtos);
    }

    public QuizListResponse.QuizDto getQuiz(Long quizId) {
        Quiz quiz = quizService.getQuizById(quizId);
        return quizMapper.toDto(quiz);
    }

    // 퀴즈 세트 생성 (CategoryDocument) - 기본 (이동시간 기준)
    @Transactional
    public void createQuizzesForDocument(Long documentId, Long userId) {
        int questionCount = calculateQuestionCount(userId);
        createQuizzesForDocument(documentId, userId, questionCount);
    }

    // 퀴즈 세트 생성 (CategoryDocument) - 문제 수 지정 (퀴즈 채우기 용)
    @Transactional
    public void createQuizzesForDocument(Long documentId, Long userId, int questionCount) {
        CategoryDocument document = categoryDocumentService.getDocumentById(documentId);
        String categoryName = document.getCategory().getName();
        String documentContent = document.getContent();

        // Goal 조회 및 검증
        Goal goal = validateGoal(document.getGoal(), userId, document.getCategory().getId());

        // Goal의 difficulty(Enum)와 prompt(String) 사용
        Difficulty difficulty = goal.getDifficulty();
        // Topic 조회
        // Topic 조회
        String topicInstruction = goalService.getTopic(userId, document.getCategory().getId());

        String path = document.getCategory().getPath();
        String description = document.getCategory().getDescription() != null ? document.getCategory().getDescription()
                : path + " " + categoryName;

        generateAndSaveQuizzes(document, categoryName, path, description, documentContent, difficulty, topicInstruction,
                questionCount);
    }

    // 퀴즈 Seeder용: 특정 문서에 대해 모든 난이도의 기본(전반적인 내용) 퀴즈가 없으면 생성
    @Transactional
    public void createDefaultQuizzesForCategoryDocument(Long documentId) {
        CategoryDocument document = categoryDocumentService.getDocumentById(documentId);
        String categoryName = document.getCategory().getName();
        String documentContent = document.getContent();

        // Seeder는 "전반적인 내용"으로 상,중,하 난이도 5문제씩 생성 (총 15문제)
        String topicInstruction = "전반적인 내용";
        int questionCount = 5;

        List<Quiz> existingQuizzes = quizService.getQuizzesByCategoryDocumentId(documentId);

        for (Difficulty difficulty : Difficulty.values()) {
            boolean exists = existingQuizzes.stream()
                    .anyMatch(q -> q.getDifficulty() == difficulty &&
                            (q.getTopic() == null || q.getTopic().equals(topicInstruction)));

            if (!exists) {
                String path = document.getCategory().getPath();
                String description = document.getCategory().getDescription() != null
                        ? document.getCategory().getDescription()
                        : path + " " + categoryName;

                generateAndSaveQuizzes(document, categoryName, path, description, documentContent, difficulty,
                        topicInstruction,
                        questionCount);
            }
        }
    }

    private static final String JSON_SCHEMA_INSTRUCTIONS = "\n반드시 다음 JSON 스키마에 맞는 '데이터만' JSON 객체로 출력하세요 (스키마 정의나 metadata 포함 금지):\n"
            + "각 필드 설명:\n"
            + "- question: 퀴즈 질문 내용\n"
            + "- options: 객관식 보기 (OX 퀴즈일 경우 'O', 'X' 포함)\n"
            + "- answer: 정답 (객관식일 경우 정답 보기의 텍스트, OX일 경우 'O' 또는 'X')\n"
            + "- type: 퀴즈 타입 ('MCQ' 또는 'OX')\n"
            + "- explanation: 정답에 대한 해설\n";

    private void executeQuizGeneration(String basePrompt, String topic, BiConsumer<LLMResponse.Quiz, String> saver) {
        BeanOutputConverter<LLMResponse> converter = new BeanOutputConverter<>(LLMResponse.class);
        String fullPrompt = basePrompt + JSON_SCHEMA_INSTRUCTIONS + converter.getFormat();

        LLMResponse response = llmService.generateAnswer(fullPrompt);
        String storedTopic = truncateTopic(topic);

        response.quizzes().forEach(quizDto -> saver.accept(quizDto, storedTopic));
    }

    private void generateAndSaveQuizzes(CategoryDocument document, String categoryName, String categoryPath,
            String categoryDescription, String documentContent,
            Difficulty difficulty, String topic, int questionCount) {
        String basePrompt = String.format(QuizPrompt.GENERATE_QUIZ.getTemplate(),
                categoryName,
                categoryPath,
                categoryDescription,
                questionCount,
                documentContent,
                difficulty,
                topic);

        executeQuizGeneration(basePrompt, topic, (quizDto, storedTopic) -> quizService.createQuizFromCategoryDocument(
                document,
                quizDto.question(),
                convertOptionsToJson(quizDto.type() == QuizType.OX ? List.of("O", "X") : quizDto.options()),
                quizDto.answer(),
                quizDto.type(),
                quizDto.explanation(),
                storedTopic,
                difficulty));
    }

    @SneakyThrows
    private String convertOptionsToJson(List<String> options) {
        if (options == null || options.isEmpty()) {
            return "[]";
        }
        return objectMapper.writeValueAsString(options);
    }

    // 퀴즈 세트 생성 (PDF Document), 파일 업로드 직후
    @Transactional
    public void createQuizzesForPdfDocument(Document document, DocumentSummary documentSummary) {
        // 사용자의 이동 시간을 기준에 따라 퀴즈 수 맞춰서 퀴즈 생성
        int questionCount = calculateQuestionCount(document.getUser().getId());

        String documentContent = document.getRawContent();

        // Goal 정보 가져오기
        Goal goal = document.getGoal();
        Difficulty difficulty = goal.getDifficulty();
        String topicInstruction = (goal.getPrompt() != null && !goal.getPrompt().isEmpty()) ? goal.getPrompt()
                : (documentSummary.getTitle() != null ? documentSummary.getTitle() : "전반적인 내용");

        String basePrompt = String.format(QuizPrompt.GENERATE_DOCUMENT_QUIZ.getTemplate(),
                questionCount,
                documentContent,
                difficulty,
                topicInstruction); // 주제 (없으면 전반적인 내용)

        executeQuizGeneration(basePrompt, topicInstruction,
                (quizDto, storedTopic) -> quizService.createQuizFromPdfDocument(
                        document,
                        documentSummary,
                        quizDto.question(),
                        convertOptionsToJson(quizDto.type() == QuizType.OX ? List.of("O", "X") : quizDto.options()),
                        quizDto.answer(),
                        quizDto.type(),
                        quizDto.explanation(),
                        storedTopic,
                        difficulty));
    }

    // 퀴즈 세트 생성 (PDF Document) - Document ID, 퀴즈 재생성
    @Transactional
    public void createQuizzesForPdfDocumentById(Long documentId, Long userId) {
        Document document = documentService.getDocumentById(documentId);
        document.validateOwner(userId);

        validateGoal(document.getGoal(), userId, null);

        // 최신 DocumentSummary 조회
        DocumentSummary summary = documentSummaryRepository.findLatestByDocumentId(documentId)
                .orElseThrow(() -> new BaseException(QuizResponseCode.NOT_FOUND_QUIZ)); // or appropriate error

        createQuizzesForPdfDocument(document, summary);
    }

    @Transactional
    public void ensureQuizzesExist(Document document, DocumentSummary summary) {
        List<Quiz> existingQuizzes = quizService.getQuizzesByDocumentSummaryId(summary.getId());
        if (existingQuizzes.isEmpty()) {
            createQuizzesForPdfDocument(document, summary);
        }
    }

    @Transactional
    public void deleteQuiz(Long quizId) {
        quizService.deleteQuiz(quizId);
    }

    public int calculateQuestionCount(Long userId) {
        if (userId == null) {
            return 10;
        }

        try {
            UserEntity user = userService.getUserById(userId);
            if (user.getCommuteInfo() == null) {
                return 10;
            }

            int usageTime = user.getCommuteInfo().getUsageTime();

            if (usageTime <= 5)
                return 3;
            if (usageTime <= 7)
                return 5;
            if (usageTime <= 10)
                return 7;
            return 10;
        } catch (Exception e) {
            return 10; // 유저 조회 실패 등 예외 시 기본값
        }
    }

    // 퀴즈 생성 전 Goal 및 퀴즈 풀이 소진 여부 검증
    private Goal validateGoal(Goal goal, Long userId, Long categoryId) {
        if (goal == null) {
            // 재사용된 공용 문서 등 Goal이 없는 경우, 해당 유저의 최신 Goal로 결정
            goal = goalService.findLatestGoal(userId, categoryId);
        }

        if (goal.getEndDate().isBefore(LocalDate.now())) {
            throw new BaseException(GoalResponseCode.GOAL_EXPIRED);
        }

        if (goal.isCompleted()) {
            throw new BaseException(GoalResponseCode.GOAL_COMPLETED);
        }

        UserEntity user = userService.getUserById(userId);
        if (user.getRole() != Role.ADMIN && !user.canSolveDailyQuiz()) {
            throw new BaseException(QuizResponseCode.TODAY_QUOTA_EXCEEDED);
        }

        return goal;
    }

    private String truncateTopic(String topic) {
        if (topic != null && topic.length() > 30) {
            return topic.substring(0, 30);
        }
        return topic;
    }
}
