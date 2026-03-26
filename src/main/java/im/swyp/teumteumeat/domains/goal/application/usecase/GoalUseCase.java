package im.swyp.teumteumeat.domains.goal.application.usecase;

import im.swyp.teumteumeat.domains.category.domain.service.CategoryService;
import im.swyp.teumteumeat.domains.category.persistence.entity.Category;
import im.swyp.teumteumeat.domains.document.domain.service.DocumentService;
import im.swyp.teumteumeat.domains.document.persistence.entity.Document;
import im.swyp.teumteumeat.domains.goal.application.dto.request.GoalCreateRequest;
import im.swyp.teumteumeat.domains.goal.application.dto.request.GoalUpdateRequest;
import im.swyp.teumteumeat.domains.goal.application.dto.response.GoalListResponse;
import im.swyp.teumteumeat.domains.goal.application.dto.response.GoalResponse;
import im.swyp.teumteumeat.domains.goal.application.mapper.GoalMapper;
import im.swyp.teumteumeat.domains.goal.domain.constant.GoalResponseCode;
import im.swyp.teumteumeat.domains.goal.domain.service.GoalService;
import im.swyp.teumteumeat.domains.goal.domain.util.PromptValidator;
import im.swyp.teumteumeat.domains.goal.persistence.entity.Goal;
import im.swyp.teumteumeat.domains.llm.domain.service.LLMService;
import im.swyp.teumteumeat.domains.user.domain.service.UserService;
import im.swyp.teumteumeat.domains.user.persistence.entity.UserEntity;
import im.swyp.teumteumeat.global.annotation.UseCase;
import im.swyp.teumteumeat.global.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.Normalizer;
import java.time.LocalDate;
import java.util.List;

@UseCase
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GoalUseCase {

    private final GoalService goalService;
    private final DocumentService documentService;
    private final CategoryService categoryService;
    private final UserService userService;
    private final LLMService llmService;

    public GoalListResponse getGoals(Long userId) {
        UserEntity user = userService.getUserById(userId);
        List<Goal> goals = goalService.getGoals(user);
        List<GoalResponse> responses = goals.stream().map(GoalMapper::fromGoal).toList();
        return GoalMapper.toGoalListResponse(responses);
    }

    @Transactional
    public Long createGoal(Long userId, GoalCreateRequest request) {
        UserEntity user = userService.getUserById(userId);

        // prompt 유효성 검증 (입력된 경우에만)
        validatePromptIfPresent(request.prompt());

        Category category = null;
        if (request.categoryId() != null) {
            category = categoryService.getCategoryById(request.categoryId());
        }
        Goal goal = GoalMapper.toGoal(user, request, category, LocalDate.now());
        Long goalId = goalService.createGoal(goal);

        // 문서 등록 요청한 경우
        String fileKey = request.fileKey();
        String fileName = request.fileName();
        if (fileKey != null && fileName != null) {
            fileKey = Normalizer.normalize(fileKey, Normalizer.Form.NFC);
            fileName = Normalizer.normalize(fileName, Normalizer.Form.NFC);
            // 이미 문서 Entity가 생성되어 있으면 가져오고, 없으면 임시 문서 생성
            Document document = documentService.getOrSaveDocument(fileKey, fileName);

            // 임시 문서인 경우 User와 Goal이 설정되어 있지 않으므로 설정
            document.updateUser(user);
            document.updateGoal(goal);
        }

        // 새로 생성된 목표를 무조건 현재 목표로 설정
        user.updateCurrentGoal(goal);

        return goalId;
    }

    @Transactional
    public void updateGoal(Long userId, Long goalId, GoalUpdateRequest request) {
        Goal goal = goalService.getGoalById(goalId);
        goal.validateOwner(userId);

        // prompt 유효성 검증 (변경된 경우에만)
        validatePromptIfPresent(request.prompt());

        goalService.updateGoal(goal, request);
    }

    /**
     * prompt가 비어있지 않은 경우에만 2단계 검증 수행
     * 1단계: 규칙 기반 1차 차단
     * 2단계: LLM 기반 판단
     */
    private void validatePromptIfPresent(String prompt) {
        if (!StringUtils.hasText(prompt)) {
            return;
        }
        // 1단계: 규칙 기반 필터
        if (PromptValidator.isBlocked(prompt)) {
            throw new BaseException(GoalResponseCode.INVALID_PROMPT);
        }
        // 2단계: LLM 기반 분류
        if (!llmService.validatePrompt(prompt)) {
            throw new BaseException(GoalResponseCode.INVALID_PROMPT);
        }
    }

    @Transactional
    public void deleteGoal(Long userId, Long goalId) {
        Goal goal = goalService.getGoalById(goalId);
        goal.validateOwner(userId);

        goalService.deleteGoal(goalId);
    }
}