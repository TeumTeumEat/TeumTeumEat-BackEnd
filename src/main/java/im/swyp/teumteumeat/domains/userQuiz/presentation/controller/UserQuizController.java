package im.swyp.teumteumeat.domains.userQuiz.presentation.controller;

import im.swyp.teumteumeat.domains.userQuiz.application.dto.request.QuizSubmissionRequest;
import im.swyp.teumteumeat.domains.userQuiz.application.dto.response.QuizSetResponse;
import im.swyp.teumteumeat.domains.userQuiz.application.dto.response.QuizSubmissionResponse;
import im.swyp.teumteumeat.domains.userQuiz.application.dto.response.UserQuizStatusResponse;
import im.swyp.teumteumeat.domains.userQuiz.application.dto.response.QuizGuideResponse;
import im.swyp.teumteumeat.domains.userQuiz.application.usecase.UserQuizUseCase;
import im.swyp.teumteumeat.domains.userQuiz.presentation.api.UserQuizApi;
import im.swyp.teumteumeat.domains.goal.domain.constant.GoalType;
import im.swyp.teumteumeat.domains.goal.persistence.entity.Goal;
import im.swyp.teumteumeat.domains.user.domain.service.UserService;
import im.swyp.teumteumeat.global.common.ApiResponse;
import im.swyp.teumteumeat.global.common.CommonResponseCode;
import im.swyp.teumteumeat.global.security.dto.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user-quizzes")
@RequiredArgsConstructor
public class UserQuizController implements UserQuizApi {

    private final UserQuizUseCase userQuizUseCase;
    private final UserService userService;

    // 유저가 퀴즈를 푸는 기능
    @Override
    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<QuizSubmissionResponse>> submitQuiz(
            @RequestBody @Valid QuizSubmissionRequest request,
            @AuthenticationPrincipal CustomUserDetails user) {
        QuizSubmissionResponse response = userQuizUseCase.submitQuiz(user.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, response));
    }

    // 퀴즈 이동시간에 따른 개수만큼 조회 (정답 미포함)
    @Override
    @GetMapping
    public ResponseEntity<ApiResponse<List<QuizSetResponse>>> getQuizzes(
            @RequestParam Long documentId,
            @RequestParam(required = false, defaultValue = "CATEGORY") GoalType documentType,
            @AuthenticationPrincipal CustomUserDetails user) {
        List<QuizSetResponse> response = userQuizUseCase.getQuizzesForSolving(documentId, user.getUserId(),
                documentType);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, response));
    }

    // 퀴즈 1개 조회 (정답 미포함)
    @Override
    @GetMapping("/{quizId}")
    public ResponseEntity<ApiResponse<QuizSetResponse>> getQuiz(
            @PathVariable Long quizId,
            @AuthenticationPrincipal CustomUserDetails user) {
        QuizSetResponse response = userQuizUseCase.getQuizForSolving(quizId);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, response));
    }

    @Override
    @PostMapping("/guide")
    public ResponseEntity<ApiResponse<QuizGuideResponse>> completeQuizGuide(
            @AuthenticationPrincipal CustomUserDetails user) {
        userQuizUseCase.completeQuizGuide(user.getUserId());
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, new QuizGuideResponse(true)));
    }

    @Override
    @PostMapping("/complete-set")
    public ResponseEntity<ApiResponse<Void>> completeQuizSet(
            @AuthenticationPrincipal CustomUserDetails user) {
        userQuizUseCase.completeQuizSet(user.getUserId());
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, null));
    }

    @Override
    @PostMapping("/ad-reward")
    public ResponseEntity<ApiResponse<Void>> claimAdReward(
            @AuthenticationPrincipal CustomUserDetails user) {
        userQuizUseCase.claimAdReward(user.getUserId());
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, null));
    }

    @Override
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<UserQuizStatusResponse>> getStatus(
            @AuthenticationPrincipal CustomUserDetails user) {
        boolean hasSolvedToday = userQuizUseCase.hasSolvedAnyQuizToday(user.getUserId());
        boolean hasSolvedEver = userQuizUseCase.hasSolvedAnyQuizEver(user.getUserId());
        boolean hasGeneratedContent = userQuizUseCase.hasCreatedDocumentToday(user.getUserId());
        boolean isQuizGuideSeen = userQuizUseCase.isQuizGuideSeen(user.getUserId());

        im.swyp.teumteumeat.domains.user.persistence.entity.UserEntity userEntity = userService
                .getUserById(user.getUserId());
        int availableQuizCount = userEntity.getAvailableQuizCount();
        int targetQuizSetCount = 0;
        int completedQuizSetCount = 0;

        Goal currentGoal = userEntity.getCurrentGoal();
        if (currentGoal != null) {
            targetQuizSetCount = (currentGoal.getTargetQuizSetCount() != null) ? currentGoal.getTargetQuizSetCount()
                    : 0;
            completedQuizSetCount = (currentGoal.getCompletedQuizSetCount() != null)
                    ? currentGoal.getCompletedQuizSetCount()
                    : 0;
        }

        if (userEntity.getRole() == im.swyp.teumteumeat.domains.user.domain.constant.Role.ADMIN) {
            hasSolvedToday = false;
            hasGeneratedContent = false;
            availableQuizCount = 999;
        }

        UserQuizStatusResponse response = UserQuizStatusResponse.builder()
                .hasSolvedToday(hasSolvedToday)
                .isFirstTime(!hasSolvedEver)
                .hasCreatedToday(hasGeneratedContent)
                .isQuizGuideSeen(isQuizGuideSeen)
                .availableQuizCount(availableQuizCount)
                .targetQuizSetCount(targetQuizSetCount)
                .completedQuizSetCount(completedQuizSetCount)
                .build();

        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, response));
    }
}
