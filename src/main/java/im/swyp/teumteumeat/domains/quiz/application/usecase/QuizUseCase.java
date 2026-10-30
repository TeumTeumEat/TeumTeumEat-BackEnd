package im.swyp.teumteumeat.domains.quiz.application.usecase;

import im.swyp.teumteumeat.domains.categoryDocument.domain.service.CategoryDocumentService;
import im.swyp.teumteumeat.domains.goal.domain.constant.Difficulty;
import im.swyp.teumteumeat.domains.categoryDocument.persistence.entity.CategoryDocument;
import im.swyp.teumteumeat.domains.document.domain.service.DocumentSectionService;
import im.swyp.teumteumeat.domains.document.domain.service.DocumentService;
import im.swyp.teumteumeat.domains.document.domain.service.DocumentSummaryService;
import im.swyp.teumteumeat.domains.document.persistence.entity.Document;
import im.swyp.teumteumeat.domains.document.persistence.entity.DocumentSummary;
import im.swyp.teumteumeat.domains.goal.persistence.entity.Goal;

import im.swyp.teumteumeat.domains.llm.application.dto.response.LLMResponse;
import im.swyp.teumteumeat.domains.llm.application.dto.response.QuizValidationResponse;
import im.swyp.teumteumeat.domains.llm.domain.prompt.QuizPrompt;
import im.swyp.teumteumeat.domains.llm.domain.service.LLMService;
import im.swyp.teumteumeat.domains.quiz.application.component.QuizGenerationMetrics;
import im.swyp.teumteumeat.domains.quiz.application.dto.response.QuizListResponse;
import im.swyp.teumteumeat.domains.quiz.application.mapper.QuizMapper;
import im.swyp.teumteumeat.domains.quiz.domain.service.QuizService;
import im.swyp.teumteumeat.domains.quiz.persistence.entity.Quiz;
import im.swyp.teumteumeat.domains.user.domain.service.UserService;
import im.swyp.teumteumeat.domains.user.persistence.entity.UserEntity;
import im.swyp.teumteumeat.global.annotation.UseCase;
import im.swyp.teumteumeat.global.component.DistributedLockFacade;
import im.swyp.teumteumeat.domains.goal.domain.service.GoalService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import java.util.function.BiFunction;
import java.util.function.IntFunction;

import im.swyp.teumteumeat.global.exception.BaseException;
import im.swyp.teumteumeat.domains.goal.domain.constant.GoalResponseCode;
import im.swyp.teumteumeat.domains.quiz.domain.constant.QuizResponseCode;
import im.swyp.teumteumeat.domains.quiz.domain.constant.QuizType;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@UseCase
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class QuizUseCase {

    private final QuizService quizService;
    private final QuizGenerationMetrics quizGenerationMetrics;
    private final DistributedLockFacade distributedLockFacade;
    private final CategoryDocumentService categoryDocumentService;
    private final LLMService llmService;
    private final QuizMapper quizMapper;
    private final ObjectMapper objectMapper;
    private final DocumentService documentService;
    private final DocumentSectionService documentSectionService;
    private final UserService userService;
    private final GoalService goalService;
    private final DocumentSummaryService documentSummaryService;

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
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void createQuizzesForDocument(Long documentId, Long userId) {
        int questionCount = calculateQuestionCount(userId);
        createQuizzesForDocument(documentId, userId, questionCount);
    }

    // 퀴즈 세트 생성 (CategoryDocument) - 문제 수 지정 (퀴즈 채우기 용)
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void createQuizzesForDocument(Long documentId, Long userId, int questionCount) {
        CategoryDocument document = categoryDocumentService.getDocumentWithCategoryById(documentId);
        String categoryName = document.getCategory().getName();
        String documentContent = document.getContent();

        // Goal 조회 및 검증
        Goal goal = validateGoal(document.getGoal(), userId, document.getCategory().getId());

        // Goal의 difficulty(Enum)와 prompt(String) 사용
        Difficulty difficulty = goal.getDifficulty();
        // Topic 조회
        String topicInstruction = goalService.getTopic(userId, document.getCategory().getId());

        String path = document.getCategory().getPath();
        String description = document.getCategory().getDescription() != null ? document.getCategory().getDescription()
                : path + " " + categoryName;

        generateAndSaveQuizzes(document, categoryName, path, description, documentContent, difficulty, topicInstruction,
                questionCount);
    }

    // 사용자가 (아직 안 푼) 퀴즈를 충분히 갖도록 보장한다 - 부족하면 부족분만 락을 걸고 생성한다.
    // 실제 풀이 조회 시점(UserQuizUseCase)과, 요약글 생성 직후 미리 당겨오는 프리페치 양쪽에서 공용으로 쓰인다.
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public List<Quiz> ensureQuizzesAvailable(Long documentId, Long userId, int quizCount) {
        CategoryDocument document = categoryDocumentService.getDocumentWithCategoryById(documentId);
        Goal goal = goalService.findLatestGoalWithCategory(userId, document.getCategory().getId());

        Difficulty targetDifficulty = goal.getDifficulty();
        String rawTopic = truncateTopic(goal.getPrompt());
        boolean isDefaultPrompt = rawTopic == null || rawTopic.isBlank();
        String targetTopic = isDefaultPrompt ? "전반적인 내용" : rawTopic;

        // 아직 풀지 않은 퀴즈가 충분히 존재하는지 확인, 존재 시 퀴즈 추가 없이 퀴즈 리스트 반환
        List<Quiz> priorityQuizzes = quizService.getUnsolvedQuizzesByAttributes(documentId, userId,
                targetDifficulty, targetTopic, quizCount);

        if (priorityQuizzes.size() >= quizCount) {
            return priorityQuizzes;
        }

        String lockKey = "lock:quiz:generation:" + documentId + ":" + userId;
        return distributedLockFacade.tryExecuteWithLock(lockKey, 30, 60, TimeUnit.SECONDS, () -> {
            // 이중 체크(Double-Check): 락 획득 후 다시 한 번 개수 확인 (그 사이 다른 요청이 이미 채웠을 수 있음)
            List<Quiz> currentQuizzes = quizService.getUnsolvedQuizzesByAttributes(documentId, userId,
                    targetDifficulty, targetTopic, quizCount);

            if (currentQuizzes.size() < quizCount) {
                int remainingCount = quizCount - currentQuizzes.size();
                createQuizzesForDocument(documentId, userId, remainingCount);

                return quizService.getUnsolvedQuizzesByAttributes(documentId, userId,
                        targetDifficulty, targetTopic, quizCount);
            }
            return currentQuizzes;
        }).orElse(priorityQuizzes);
    }

    // 퀴즈 Seeder용: 특정 문서에 대해 모든 난이도의 기본(전반적인 내용) 퀴즈가 없으면 생성
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void createDefaultQuizzesForCategoryDocument(Long documentId) {
        CategoryDocument document = categoryDocumentService.getDocumentWithCategoryById(documentId);
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

    // BeanOutputConverter는 생성 시 리플렉션으로 JSON 스키마를 만들고, 스키마는 고정이므로 포맷 문구를 한 번만 만들어 재사용한다
    private static final String GENERATION_OUTPUT_FORMAT = JSON_SCHEMA_INSTRUCTIONS
            + new BeanOutputConverter<>(LLMResponse.class).getFormat();

    // 검증으로 걸러진 만큼 부족분을 다시 생성해 채우되, 무한 재시도를 막기 위해 시도 횟수를 제한한다
    private static final int MAX_GENERATION_ATTEMPTS = 3;

    private void executeQuizGeneration(int questionCount, IntFunction<String> promptBuilder, String topic,
            BiFunction<LLMResponse.Quiz, String, Quiz> quizBuilder) {
        String storedTopic = truncateTopic(topic);
        List<LLMResponse.Quiz> collectedQuizzes = new ArrayList<>();

        long startNanos = System.nanoTime();
        int remainingCount = questionCount;
        int attempt = 0;
        while (attempt < MAX_GENERATION_ATTEMPTS && remainingCount > 0) {
            attempt++;
            String fullPrompt = promptBuilder.apply(remainingCount) + GENERATION_OUTPUT_FORMAT;

            LLMResponse response = llmService.generateAnswer(fullPrompt);
            collectedQuizzes.addAll(filterByValidation(response.quizzes()));
            remainingCount = questionCount - collectedQuizzes.size();

            if (remainingCount > 0 && attempt < MAX_GENERATION_ATTEMPTS) {
                log.warn("[퀴즈 생성 부족분 재시도] 목표={}, 확보={}, 부족={}, 다음 시도={}",
                        questionCount, collectedQuizzes.size(), remainingCount, attempt + 1);
            }
        }
        quizGenerationMetrics.recordGeneration(Duration.ofNanos(System.nanoTime() - startNanos), attempt);

        List<Quiz> quizzes = collectedQuizzes.stream()
                .limit(questionCount)
                .map(quizDto -> quizBuilder.apply(quizDto, storedTopic))
                .toList();
        quizService.saveQuizzes(quizzes);
    }

    private static final String VALIDATION_JSON_SCHEMA_INSTRUCTIONS = "\n반드시 다음 JSON 스키마에 맞는 '데이터만' JSON 객체로 출력하세요 "
            + "(스키마 정의나 metadata 포함 금지, 근거 설명 없이 판정 결과만 간결하게):\n";

    private static final String VALIDATION_OUTPUT_FORMAT = VALIDATION_JSON_SCHEMA_INSTRUCTIONS
            + new BeanOutputConverter<>(QuizValidationResponse.class).getFormat();

    // 생성된 퀴즈 세트를 별도 LLM 콜로 재검증해, 문제/정답/해설이 논리적으로 모순되는 항목을 걸러낸다 (사전 검증)
    // 검증은 품질 보조 장치이므로, 검증 콜 자체가 실패(429, 5xx 등)하면 이미 생성된 퀴즈를 버리지 않고 검증 없이 통과시킨다
    private List<LLMResponse.Quiz> filterByValidation(List<LLMResponse.Quiz> quizzes) {
        if (quizzes.isEmpty()) {
            return quizzes;
        }

        Set<Integer> invalidIndexes;
        try {
            invalidIndexes = findInvalidIndexes(quizzes);
        } catch (Exception e) {
            log.warn("[퀴즈 검증 건너뜀] 검증 콜 실패로 {}개 퀴즈를 검증 없이 통과시킵니다", quizzes.size(), e);
            quizGenerationMetrics.recordValidationSkipped(quizzes.size());
            return quizzes;
        }

        quizGenerationMetrics.recordValidationChecked(quizzes.size());
        quizGenerationMetrics.recordValidationInvalid(invalidIndexes.size());

        if (!invalidIndexes.isEmpty()) {
            log.warn("[퀴즈 검증 실패] 무효 판정되어 제외된 인덱스={}", invalidIndexes);
        }

        return IntStream.range(0, quizzes.size())
                .filter(i -> !invalidIndexes.contains(i))
                .mapToObj(quizzes::get)
                .toList();
    }

    private Set<Integer> findInvalidIndexes(List<LLMResponse.Quiz> quizzes) {
        QuizValidationResponse validationResponse = llmService.validateQuizzes(buildValidationPrompt(quizzes));

        return validationResponse.results().stream()
                .filter(result -> !result.isValid())
                .map(QuizValidationResponse.Result::index)
                .collect(Collectors.toSet());
    }

    private String buildValidationPrompt(List<LLMResponse.Quiz> quizzes) {
        StringBuilder quizListBuilder = new StringBuilder();
        for (int i = 0; i < quizzes.size(); i++) {
            LLMResponse.Quiz quiz = quizzes.get(i);
            quizListBuilder.append(i).append(". 문제: ").append(quiz.question())
                    .append(" / 정답: ").append(quiz.answer())
                    .append(" / 해설: ").append(quiz.explanation())
                    .append("\n");
        }

        return String.format(QuizPrompt.VALIDATE_QUIZ.getTemplate(), quizListBuilder) + VALIDATION_OUTPUT_FORMAT;
    }

    private void generateAndSaveQuizzes(CategoryDocument document, String categoryName, String categoryPath,
            String categoryDescription, String documentContent,
            Difficulty difficulty, String topic, int questionCount) {
        IntFunction<String> promptBuilder = count -> String.format(QuizPrompt.GENERATE_QUIZ.getTemplate(),
                categoryName,
                categoryPath,
                categoryDescription,
                count,
                documentContent,
                difficulty,
                topic);

        executeQuizGeneration(questionCount, promptBuilder, topic, (quizDto, storedTopic) -> quizService.buildQuizFromCategoryDocument(
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
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void createQuizzesForPdfDocument(Document document, DocumentSummary documentSummary) {
        // 비동기 콜백으로 전달된 document는 이전 트랜잭션(세션)에서 분리된 상태이고, 이 메서드
        // 자체도 NOT_SUPPORTED라 세션이 없으므로 Goal/User를 lazy 로딩하지 않도록 fetch join 조회 사용
        Document attachedDocument = documentService.getDocumentWithGoalAndUserById(document.getId());

        // 사용자의 이동 시간을 기준에 따라 퀴즈 수 맞춰서 퀴즈 생성
        int questionCount = calculateQuestionCount(attachedDocument.getUser().getId());

        // Goal 정보 가져오기
        Goal goal = attachedDocument.getGoal();
        String documentContent = documentSectionService.resolveCurrentSectionContent(attachedDocument.getId(), goal);

        Difficulty difficulty = goal.getDifficulty();
        String topicInstruction = (goal.getPrompt() != null && !goal.getPrompt().isEmpty()) ? goal.getPrompt()
                : (documentSummary.getTitle() != null ? documentSummary.getTitle() : "전반적인 내용");

        IntFunction<String> promptBuilder = count -> String.format(QuizPrompt.GENERATE_DOCUMENT_QUIZ.getTemplate(),
                count,
                documentContent,
                difficulty,
                topicInstruction); // 주제 (없으면 전반적인 내용)

        executeQuizGeneration(questionCount, promptBuilder, topicInstruction,
                (quizDto, storedTopic) -> quizService.buildQuizFromPdfDocument(
                        attachedDocument,
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
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void createQuizzesForPdfDocumentById(Long documentId, Long userId) {
        Document document = documentService.getDocumentWithGoalAndUserById(documentId);
        document.validateOwner(userId);

        validateGoal(document.getGoal(), userId, null);

        // 최신 DocumentSummary 조회
        DocumentSummary summary = documentSummaryService.getLatestSummaryByDocumentId(documentId)
                .orElseThrow(() -> new BaseException(QuizResponseCode.NOT_FOUND_QUIZ)); // or appropriate error

        createQuizzesForPdfDocument(document, summary);
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

        if (goal.isCompleted()) {
            throw new BaseException(GoalResponseCode.GOAL_COMPLETED);
        }

        UserEntity user = userService.getUserById(userId);
        if (!user.canSolveDailyQuiz()) {
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
