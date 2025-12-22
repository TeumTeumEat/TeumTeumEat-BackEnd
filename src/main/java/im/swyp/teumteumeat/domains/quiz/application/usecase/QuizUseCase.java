package im.swyp.teumteumeat.domains.quiz.application.usecase;

import im.swyp.teumteumeat.domains.categoryDocument.domain.service.CategoryDocumentService;
import im.swyp.teumteumeat.domains.goal.domain.constant.Difficulty;
import im.swyp.teumteumeat.domains.categoryDocument.persistence.entity.CategoryDocument;
import im.swyp.teumteumeat.domains.document.domain.service.DocumentService;
import im.swyp.teumteumeat.domains.document.persistence.entity.Document;
import im.swyp.teumteumeat.domains.goal.persistence.entity.Goal;
import im.swyp.teumteumeat.domains.goal.persistence.repository.GoalRepository;
import im.swyp.teumteumeat.domains.llm.application.dto.response.LLMResponse;
import im.swyp.teumteumeat.domains.llm.domain.prompt.QuizPrompt;
import im.swyp.teumteumeat.domains.llm.domain.service.LLMService;
import im.swyp.teumteumeat.domains.quiz.application.dto.response.QuizListResponse;
import im.swyp.teumteumeat.domains.quiz.application.mapper.QuizMapper;
import im.swyp.teumteumeat.domains.quiz.domain.service.QuizService;
import im.swyp.teumteumeat.domains.quiz.persistence.entity.Quiz;
import im.swyp.teumteumeat.domains.user.domain.service.UserService;
import im.swyp.teumteumeat.domains.user.persistence.entity.UserEntity;
import im.swyp.teumteumeat.global.annotation.UseCase;
import im.swyp.teumteumeat.global.common.CommonResponseCode;
import im.swyp.teumteumeat.global.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

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
    private final GoalRepository goalRepository;

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

        // Goal 조회 (해당 유저/카테고리의 최신 목표)
        var goal = goalRepository
                .findTopByUserIdAndCategoryIdOrderByCreatedDateDesc(userId, document.getCategory().getId())
                .orElseThrow(() -> new BaseException(
                        CommonResponseCode.NOT_FOUND)); // 적절한 예외 처리 필요

        // Goal의 difficulty(Enum)와 prompt(String) 사용
        Difficulty difficulty = goal.getDifficulty();
        String topicInstruction = goal.getPrompt();

        generateAndSaveQuizzes(document, categoryName, documentContent, difficulty, topicInstruction,
                questionCount);
    }

    private void generateAndSaveQuizzes(CategoryDocument document, String categoryName, String documentContent,
            Difficulty difficulty, String topic, int questionCount) {
        BeanOutputConverter<LLMResponse> converter = new BeanOutputConverter<>(LLMResponse.class);
        String topicInstruction = (topic != null && !topic.isEmpty()) ? topic : "전반적인 내용";

        // 프롬프트 메시지 구성
        String promptMessage = String.format(QuizPrompt.GENERATE_QUIZ.getTemplate(),
                categoryName,
                questionCount,
                documentContent,
                difficulty,
                topicInstruction) // 주제 (없으면 전반적인 내용)
                + "\n반드시 다음 JSON 스키마에 맞는 '데이터만' JSON 객체로 출력하세요 (스키마 정의나 metadata 포함 금지):\n" + converter.getFormat();

        LLMResponse response = llmService.generateAnswer(promptMessage);

        // Topic 저장 시 길이 제한 (30자)
        String storedTopic = (topic != null && topic.length() > 30) ? topic.substring(0, 30) : topic;

        response.quizzes().forEach(quizDto -> {
            quizService.createQuizFromCategoryDocument(
                    document,
                    quizDto.question(),
                    convertOptionsToJson(quizDto.options()),
                    quizDto.answer(),
                    quizDto.type(),
                    quizDto.explanation(),
                    storedTopic,
                    difficulty);
        });

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
    public void createQuizzesForPdfDocument(Document document) {
        // 사용자의 이동 시간을 기준에 따라 퀴즈 수 맞춰서 퀴즈 생성
        int questionCount = calculateQuestionCount(document.getGoal().getUser().getId());

        String documentContent = document.getRawContent();

        // Goal 정보 가져오기
        var goal = document.getGoal();
        Difficulty difficulty = goal.getDifficulty();
        String topicInstruction = (goal.getPrompt() != null && !goal.getPrompt().isEmpty()) ? goal.getPrompt()
                : "전반적인 내용";

        BeanOutputConverter<LLMResponse> converter = new BeanOutputConverter<>(LLMResponse.class);

        String prompt = String.format(QuizPrompt.GENERATE_DOCUMENT_QUIZ.getTemplate(),
                questionCount,
                documentContent,
                difficulty,
                topicInstruction) // 주제 (없으면 전반적인 내용)
                + "\n반드시 다음 JSON 스키마에 맞는 '데이터만' JSON 객체로 출력하세요 (스키마 정의나 metadata 포함 금지):\n"
                + converter.getFormat();
        LLMResponse response = llmService.generateAnswer(prompt);

        // Topic 저장 시 길이 제한 (30자)
        String storedTopic = (topicInstruction != null && topicInstruction.length() > 30)
                ? topicInstruction.substring(0, 30)
                : topicInstruction;

        response.quizzes().forEach(quizDto -> {
            quizService.createQuizFromPdfDocument(
                    document,
                    quizDto.question(),
                    convertOptionsToJson(quizDto.options()),
                    quizDto.answer(),
                    quizDto.type(),
                    quizDto.explanation(),
                    storedTopic,
                    difficulty);
        });
    }

    // 퀴즈 세트 생성 (PDF Document) - Document ID, 퀴즈 재생성
    @Transactional
    public void createQuizzesForPdfDocumentById(Long documentId) {
        Document document = documentService.getDocumentById(documentId);
        createQuizzesForPdfDocument(document);
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
            var user = userService.getUserById(userId);
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
}
