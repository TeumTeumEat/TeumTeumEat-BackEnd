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
import im.swyp.teumteumeat.domains.goal.domain.service.GoalService;
import im.swyp.teumteumeat.domains.goal.persistence.entity.Goal;
import im.swyp.teumteumeat.domains.user.domain.service.UserService;
import im.swyp.teumteumeat.domains.user.persistence.entity.UserEntity;
import im.swyp.teumteumeat.global.annotation.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

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

    public GoalListResponse getGoals(Long userId) {
        UserEntity user = userService.getUserById(userId);
        List<Goal> goals = goalService.getGoals(user);
        List<GoalResponse> responses = goals.stream().map(GoalMapper::fromGoal).toList();
        return GoalMapper.toGoalListResponse(responses);
    }

    @Transactional
    public Long createGoal(Long userId, GoalCreateRequest request) {
        UserEntity user = userService.getUserById(userId);

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
            // 이미 문서 Entity가 생성되어 있으면 가져오고, 없으면 임시 문서 생성
            Document document = documentService.getOrSaveDocument(fileKey, fileName);

            // 임시 문서인 경우 User와 Goal이 설정되어 있지 않으므로 설정
            document.updateUser(user);
            document.updateGoal(goal);
        }

        return goalId;
    }

    @Transactional
    public void updateGoal(Long userId, Long goalId, GoalUpdateRequest request) {
        Goal goal = goalService.getGoalById(goalId);
        goal.validateOwner(userId);

        goalService.updateGoal(goal, request);
    }

    @Transactional
    public void deleteGoal(Long userId, Long goalId) {
        Goal goal = goalService.getGoalById(goalId);
        goal.validateOwner(userId);

        goalService.deleteGoal(goalId);
    }
}
