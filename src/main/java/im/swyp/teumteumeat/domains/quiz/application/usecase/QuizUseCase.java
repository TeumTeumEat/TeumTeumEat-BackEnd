package im.swyp.teumteumeat.domains.quiz.application.usecase;

import im.swyp.teumteumeat.domains.categoryDocument.domain.service.CategoryDocumentService;
import im.swyp.teumteumeat.domains.categoryDocument.persistence.entity.CategoryDocument;
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

import java.util.List;

@UseCase
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuizUseCase {

    private final QuizService quizService;
    private final CategoryDocumentService categoryDocumentService;
    private final LLMService llmService;
    private final QuizMapper quizMapper;

    public QuizListResponse getQuizzes(Long documentId) {
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
    public void createQuizzesForDocument(Long documentId) {
        CategoryDocument document = categoryDocumentService.getDocumentById(documentId);
        String categoryName = document.getCategory().getName();
        String documentContent = document.getContent();

        generateAndSaveQuizzes(document, categoryName, documentContent);
    }

    private void generateAndSaveQuizzes(CategoryDocument document, String categoryName, String documentContent) {
        BeanOutputConverter<LLMResponse> converter = new BeanOutputConverter<>(LLMResponse.class);

        // 프롬프트 메시지 구성
        String promptMessage = String.format(QuizPrompt.GENERATE_QUIZ.getTemplate(),
                categoryName,
                documentContent,
                3) // 난이도
                + "\n반드시 다음 JSON 스키마에 맞는 '데이터만' JSON 객체로 출력하세요 (스키마 정의나 metadata 포함 금지):\n" + converter.getFormat();

        LLMResponse response = llmService.generateAnswer(promptMessage);

        response.quizzes().forEach(quizDto -> {
            quizService.createQuizFromCategoryDocument(
                    document,
                    quizDto.question(),
                    quizDto.options() != null ? String.join(",", quizDto.options()) : "",
                    quizDto.answer(),
                    quizDto.type(),
                    quizDto.explanation());
        });
    }

    @Transactional
    public void deleteQuiz(Long quizId) {
        quizService.deleteQuiz(quizId);
    }
}
