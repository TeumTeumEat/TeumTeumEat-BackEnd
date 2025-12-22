package im.swyp.teumteumeat.domains.quiz.application.usecase;

import im.swyp.teumteumeat.domains.categoryDocument.domain.service.CategoryDocumentService;
import im.swyp.teumteumeat.domains.categoryDocument.persistence.entity.CategoryDocument;
import im.swyp.teumteumeat.domains.document.domain.service.DocumentService;
import im.swyp.teumteumeat.domains.document.persistence.entity.Document;
import im.swyp.teumteumeat.domains.llm.application.dto.response.LLMResponse;
import im.swyp.teumteumeat.domains.llm.domain.prompt.QuizPrompt;
import im.swyp.teumteumeat.domains.llm.domain.service.LLMService;
import im.swyp.teumteumeat.domains.quiz.application.dto.response.QuizListResponse;
import im.swyp.teumteumeat.domains.quiz.application.mapper.QuizMapper;
import im.swyp.teumteumeat.domains.quiz.domain.service.QuizService;
import im.swyp.teumteumeat.domains.quiz.persistence.entity.Quiz;
import im.swyp.teumteumeat.global.annotation.UseCase;
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

    // 퀴즈 세트 생성 (CategoryDocument)
    @Transactional
    public void createQuizzesForDocument(Long documentId, int difficulty, String topic) {
        CategoryDocument document = categoryDocumentService.getDocumentById(documentId);
        String categoryName = document.getCategory().getName();
        String documentContent = document.getContent();

        generateAndSaveQuizzes(document, categoryName, documentContent, difficulty, topic);
    }

    private void generateAndSaveQuizzes(CategoryDocument document, String categoryName, String documentContent,
            int difficulty, String topic) {
        BeanOutputConverter<LLMResponse> converter = new BeanOutputConverter<>(LLMResponse.class);
        String topicInstruction = (topic != null && !topic.isEmpty()) ? topic : "전반적인 내용";

        // 프롬프트 메시지 구성
        String promptMessage = String.format(QuizPrompt.GENERATE_QUIZ.getTemplate(),
                categoryName,
                documentContent,
                difficulty,
                topicInstruction) // 주제 (없으면 전반적인 내용)
                + "\n반드시 다음 JSON 스키마에 맞는 '데이터만' JSON 객체로 출력하세요 (스키마 정의나 metadata 포함 금지):\n" + converter.getFormat();

        LLMResponse response = llmService.generateAnswer(promptMessage);

        response.quizzes().forEach(quizDto -> {
            quizService.createQuizFromCategoryDocument(
                    document,
                    quizDto.question(),
                    convertOptionsToJson(quizDto.options()),
                    quizDto.answer(),
                    quizDto.type(),
                    quizDto.explanation(),
                    topicInstruction);
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
    public void createQuizzesForPdfDocument(Document document, int difficulty, String topic) {
        String documentContent = document.getRawContent();
        String topicInstruction = (topic != null && !topic.isEmpty()) ? topic : "전반적인 내용";

        BeanOutputConverter<LLMResponse> converter = new BeanOutputConverter<>(LLMResponse.class);

        String prompt = String.format(QuizPrompt.GENERATE_DOCUMENT_QUIZ.getTemplate(), documentContent,
                difficulty,
                topicInstruction) // 주제 (없으면 전반적인 내용)
                + "\n반드시 다음 JSON 스키마에 맞는 '데이터만' JSON 객체로 출력하세요 (스키마 정의나 metadata 포함 금지):\n"
                + converter.getFormat();
        LLMResponse response = llmService.generateAnswer(prompt);

        response.quizzes().forEach(quizDto -> {
            quizService.createQuizFromPdfDocument(
                    document,
                    quizDto.question(),
                    convertOptionsToJson(quizDto.options()),
                    quizDto.answer(),
                    quizDto.type(),
                    quizDto.explanation(),
                    topicInstruction);
        });
    }

    // 퀴즈 세트 생성 (PDF Document) - Document ID, 퀴즈 재생성
    @Transactional
    public void createQuizzesForPdfDocumentById(Long documentId, int difficulty, String topic) {
        Document document = documentService.getDocumentById(documentId);
        createQuizzesForPdfDocument(document, difficulty, topic);
    }

    @Transactional
    public void deleteQuiz(Long quizId) {
        quizService.deleteQuiz(quizId);
    }
}
