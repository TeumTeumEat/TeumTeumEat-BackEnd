package im.swyp.teumteumeat.global.security.controller;

import im.swyp.teumteumeat.domains.user.persistence.entity.UserEntity;
import im.swyp.teumteumeat.domains.user.persistence.repository.UserRepository;
import im.swyp.teumteumeat.global.common.ApiResponse;
import im.swyp.teumteumeat.global.common.CommonResponseCode;
import im.swyp.teumteumeat.global.security.constant.SocialProvider;
import im.swyp.teumteumeat.global.security.dto.AuthTokenResponse;
import im.swyp.teumteumeat.global.security.dto.GoogleLoginRequest;
import im.swyp.teumteumeat.global.security.service.GoogleAuthService;
import im.swyp.teumteumeat.global.security.service.KakaoAuthService;
import im.swyp.teumteumeat.global.security.token.JwtProvider;
import im.swyp.teumteumeat.global.security.token.Token;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "인증 관련 API")
public class AuthController {

    private final KakaoAuthService kakaoAuthService;
    private final GoogleAuthService googleAuthService;
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    @SuppressWarnings("unchecked")
    @Operation(summary = "카카오 소셜 로그인 (id_token 직접 검증)", description = "Android 등에서 발급받은 id_token을 전송하면 서버에서 검증 후 액세스/리프레시 토큰을 발급합니다.")
    @PostMapping("/kakao")
    public ApiResponse<AuthTokenResponse> kakaoLogin(@RequestBody GoogleLoginRequest request) {
        // 1. 카카오 ID Token 검증
        Map<String, Object> payload = kakaoAuthService.verifyIdToken(request.idToken());

        Map<String, Object> kakaoAccount = (Map<String, Object>) payload.get("kakao_account");
        Map<String, Object> profile = (kakaoAccount != null) ? (Map<String, Object>) kakaoAccount.get("profile") : null;

        String socialId = String.valueOf(payload.get("id"));
        String email = (kakaoAccount != null) ? (String) kakaoAccount.get("email") : null;
        String name = (profile != null) ? (String) profile.get("nickname") : null;

        log.info("Kakao Login Success - socialId: {}, email: {}", socialId, email);

        // 2. 가입 여부 확인 및 회원가입/로그인 처리
        UserEntity user = userRepository.findBySocialProviderAndSocialId(SocialProvider.KAKAO, socialId)
                .orElseGet(() -> userRepository
                        .save(UserEntity.socialSignup(name, email, SocialProvider.KAKAO, socialId)));

        // 3. 토큰 발급
        Token token = jwtProvider.issueToken(user);

        return ApiResponse.ofSuccess(CommonResponseCode.OK, AuthTokenResponse.builder()
                .accessToken(token.accessToken())
                .refreshToken(token.refreshToken())
                .build());
    }

    @Operation(summary = "구글 소셜 로그인 (id_token 직접 검증)", description = "Android 등에서 발급받은 id_token을 전송하면 서버에서 검증 후 액세스/리프레시 토큰을 발급합니다.")
    @PostMapping("/google")
    public ApiResponse<AuthTokenResponse> googleLogin(@RequestBody GoogleLoginRequest request) {
        // 1. 구글 ID Token 검증
        Map<String, Object> payload = googleAuthService.verifyIdToken(request.idToken());

        String socialId = (String) payload.get("sub");
        String email = (String) payload.get("email");
        String name = (String) payload.get("name");

        log.info("Google Login Success - socialId: {}, email: {}", socialId, email);

        // 2. 가입 여부 확인 및 회원가입/로그인 처리
        UserEntity user = userRepository.findBySocialProviderAndSocialId(SocialProvider.GOOGLE, socialId)
                .orElseGet(() -> userRepository
                        .save(UserEntity.socialSignup(name, email, SocialProvider.GOOGLE, socialId)));

        // 3. 토큰 발급
        Token token = jwtProvider.issueToken(user);

        return ApiResponse.ofSuccess(CommonResponseCode.OK, AuthTokenResponse.builder()
                .accessToken(token.accessToken())
                .refreshToken(token.refreshToken())
                .build());
    }
}
