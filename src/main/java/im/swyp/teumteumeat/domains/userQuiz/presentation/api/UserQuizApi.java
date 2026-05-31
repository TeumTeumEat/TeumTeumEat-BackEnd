package im.swyp.teumteumeat.domains.userQuiz.presentation.api;

import im.swyp.teumteumeat.domains.categoryDocument.domain.constant.CategoryDocumentResponseCode;
import im.swyp.teumteumeat.domains.goal.domain.constant.GoalType;
import im.swyp.teumteumeat.domains.quiz.domain.constant.QuizResponseCode;
import im.swyp.teumteumeat.domains.userQuiz.application.dto.request.QuizSubmissionRequest;
import im.swyp.teumteumeat.domains.userQuiz.application.dto.response.QuizGuideResponse;
import im.swyp.teumteumeat.domains.userQuiz.application.dto.response.QuizSetResponse;
import im.swyp.teumteumeat.domains.userQuiz.application.dto.response.QuizSubmissionResponse;
import im.swyp.teumteumeat.domains.userQuiz.application.dto.response.UserQuizStatusResponse;
import im.swyp.teumteumeat.global.annotation.swagger.ApiErrorResponseExplanation;
import im.swyp.teumteumeat.global.annotation.swagger.ApiResponseExplanations;
import im.swyp.teumteumeat.global.annotation.swagger.ApiSuccessResponseExplanation;
import im.swyp.teumteumeat.global.common.ApiResponse;
import im.swyp.teumteumeat.global.security.annotation.LoginUser;
import im.swyp.teumteumeat.global.security.dto.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "UserQuiz", description = "유저 퀴즈 API")
public interface UserQuizApi {

        @Operation(summary = "퀴즈 풀이 결과 제출",
                   description = """
                                 사용자가 푼 퀴즈의 정답 여부를 서버에 제출하고 결과를 저장합니다.
                                 
                                 **처리 내용**
                                 - 제출된 답안이 정답인지 오답인지 기록됩니다.
                                 - 성공 시 제출 결과(정답/오답 및 관련 피드백 등)를 반환합니다.
                                 """
        )
        @ApiResponseExplanations(
                success = @ApiSuccessResponseExplanation(responseClass = QuizSubmissionResponse.class, description = "제출 성공"),
                errors = {
                        @ApiErrorResponseExplanation(exceptionCode = QuizResponseCode.class, name = "NOT_FOUND_QUIZ")
                }
        )
        ResponseEntity<ApiResponse<QuizSubmissionResponse>> submitQuiz(
                        @RequestBody @Valid QuizSubmissionRequest request,
                        @Parameter(hidden = true) @LoginUser CustomUserDetails user);

        @Operation(summary = "퀴즈 세트 조회 (정답 미포함)",
                   description = """
                                 사용자의 이동시간 및 현재 Goal(난이도, 프롬프트) 설정에 맞춰 풀어야 할 퀴즈 세트를 조회합니다.
                                 
                                 **특징**
                                 - 아직 풀지 않은 퀴즈이기 때문에 응답에 정답은 포함되지 않습니다.
                                 - documentType(CATEGORY, DOCUMENT)에 따라 조회할 자료의 종류를 구분합니다.
                                 """
        )
        @ApiResponseExplanations(
                success = @ApiSuccessResponseExplanation(responseClass = QuizSetResponse.class, description = "조회 성공"),
                errors = {
                        @ApiErrorResponseExplanation(exceptionCode = CategoryDocumentResponseCode.class, name = "NOT_FOUND_CATEGORY_DOCUMENT")
                }
        )
        ResponseEntity<ApiResponse<List<QuizSetResponse>>> getQuizzes(
                        @RequestParam Long documentId,
                        @RequestParam(required = false, defaultValue = "CATEGORY") GoalType documentType,
                        @Parameter(hidden = true) @LoginUser CustomUserDetails user);

        @Operation(summary = "퀴즈 1개 조회 (정답 미포함)",
                   description = """
                                 특정 퀴즈 1개의 상세 정보(문제, 보기 등)를 조회합니다.
                                 
                                 **특징**
                                 - 퀴즈 진행 화면 등에서 한 문제씩 보여줄 때 사용됩니다.
                                 - 정답 정보는 포함되지 않습니다.
                                 """
        )
        @ApiResponseExplanations(
                success = @ApiSuccessResponseExplanation(description = "조회 성공"),
                errors = {
                        @ApiErrorResponseExplanation(exceptionCode = QuizResponseCode.class, name = "NOT_FOUND_QUIZ")
                }
        )
        ResponseEntity<ApiResponse<QuizSetResponse>> getQuiz(
                        @PathVariable Long quizId);

        @Operation(summary = "퀴즈 안내 가이드 확인 처리",
                   description = """
                                 사용자가 퀴즈 시작 전 안내 가이드를 확인했음을 서버에 저장합니다.
                                 
                                 **특징**
                                 - '다시 보지 않기' 등의 상태를 저장하여 이후에는 가이드가 뜨지 않게 할 때 유용합니다.
                                 """
        )
        @ApiResponseExplanations(success = @ApiSuccessResponseExplanation(responseClass = QuizGuideResponse.class, description = "처리 성공"))
        ResponseEntity<ApiResponse<QuizGuideResponse>> completeQuizGuide(
                        @Parameter(hidden = true) @LoginUser CustomUserDetails user);

        @Operation(summary = "유저 퀴즈/요약글 상태 조회",
                   description = """
                                 유저의 오늘 학습 진행 상태를 전반적으로 조회합니다.
                                 
                                 **반환 항목**
                                 - 오늘 퀴즈 풀이 여부
                                 - 최초 풀이 여부
                                 - 오늘 요약글 생성 여부
                                 
                                 **용도**
                                 - 앱 진입 시 홈/인트로 화면에서 UI 분기 처리를 위해 사용됩니다.
                                 """
        )
        @ApiResponseExplanations(success = @ApiSuccessResponseExplanation(responseClass = UserQuizStatusResponse.class, description = "조회 성공"))
        ResponseEntity<ApiResponse<UserQuizStatusResponse>> getStatus(
                        @Parameter(hidden = true) @LoginUser CustomUserDetails user);

        @Operation(summary = "퀴즈 세트 완료 처리",
                   description = """
                                 현재 진행 중인 퀴즈 세트를 모두 완료했음을 서버에 알립니다.
                                 
                                 **처리 내용**
                                 - 오늘 남은 퀴즈 풀이 횟수가 1회 차감됩니다.
                                 - 목표(Goal) 달성도가 증가합니다.
                                 """
        )
        @ApiResponseExplanations(
                success = @ApiSuccessResponseExplanation(description = "처리 성공"),
                errors = {
                        @ApiErrorResponseExplanation(exceptionCode = QuizResponseCode.class, name = "TODAY_QUOTA_EXCEEDED")
                })
        ResponseEntity<ApiResponse<Void>> completeQuizSet(
                        @Parameter(hidden = true) @LoginUser CustomUserDetails user);

        @Operation(summary = "광고 시청 보상 획득",
                   description = """
                                 사용자가 광고 시청을 완료한 후 호출하여 퀴즈 풀이 가능 횟수를 1회 추가합니다.
                                 
                                 **제약 사항**
                                 - 하루 최대 광고 시청 횟수를 초과할 경우 DAILY_AD_REWARD_LIMIT_EXCEEDED 에러가 발생합니다.
                                 """
        )
        @ApiResponseExplanations(
                success = @ApiSuccessResponseExplanation(description = "처리 성공"),
                errors = {
                        @ApiErrorResponseExplanation(exceptionCode = QuizResponseCode.class, name = "DAILY_AD_REWARD_LIMIT_EXCEEDED")
                })
        ResponseEntity<ApiResponse<Void>> claimAdReward(
                        @Parameter(hidden = true) @LoginUser CustomUserDetails user);

        @Operation(summary = "쿠폰 상태 초기화 (테스트용)",
                   description = """
                                 광고 시청 횟수 및 퀴즈 풀이 횟수 상태를 초기화합니다.
                                 
                                 **권한**
                                 - 관리자(ADMIN) 전용 API입니다.
                                 - 기능 테스트 시 초기 상태로 되돌리기 위해 사용됩니다.
                                 """
        )
        @ApiResponseExplanations(
                success = @ApiSuccessResponseExplanation(description = "처리 성공")
        )
        ResponseEntity<ApiResponse<Void>> testResetAdReward(
                        @Parameter(hidden = true) @LoginUser CustomUserDetails user);

        @Operation(summary = "퀴즈 풀이 가능 횟수 강제 추가 (테스트용)",
                   description = """
                                 광고 시청 등의 조건 없이 퀴즈 풀이 가능 횟수를 강제로 추가합니다.
                                 
                                 **권한**
                                 - 관리자(ADMIN) 전용 API입니다.
                                 - count 파라미터로 추가할 횟수를 지정합니다 (기본값: 1).
                                 """
        )
        @ApiResponseExplanations(
                success = @ApiSuccessResponseExplanation(description = "처리 성공")
        )
        ResponseEntity<ApiResponse<Void>> testAddQuizCount(
                        @RequestParam(defaultValue = "1") int count,
                        @Parameter(hidden = true) @LoginUser CustomUserDetails user);

        @Operation(summary = "현재 목표 완료/만료 상태 초기화 (테스트용)",
                   description = """
                                 목표의 상태를 테스트하기 위해 완료/만료 상태를 초기화합니다.
                                 
                                 **처리 내용**
                                 - 목표의 완료 상태가 해제됩니다.
                                 - 만약 목표가 만료되었다면 기간이 일주일 연장됩니다.
                                 
                                 **권한**
                                 - 관리자(ADMIN) 전용 API입니다.
                                 """
        )
        @ApiResponseExplanations(
                success = @ApiSuccessResponseExplanation(description = "처리 성공")
        )
        ResponseEntity<ApiResponse<Void>> testResetGoalStatus(
                        @RequestParam(required = false) @Parameter(description = "리셋할 목표 ID. 안 주면 현재 목표 리셋") Long goalId,
                        @Parameter(hidden = true) @LoginUser CustomUserDetails user);
}
