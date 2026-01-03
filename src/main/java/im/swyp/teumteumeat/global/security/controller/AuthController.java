package im.swyp.teumteumeat.global.security.controller;

import im.swyp.teumteumeat.domains.user.persistence.entity.UserEntity;
import im.swyp.teumteumeat.domains.user.persistence.repository.UserRepository;
import im.swyp.teumteumeat.global.common.ApiResponse;
import im.swyp.teumteumeat.global.common.CommonResponseCode;
import im.swyp.teumteumeat.global.security.api.AuthApi;
import im.swyp.teumteumeat.global.security.constant.SocialProvider;
import im.swyp.teumteumeat.global.security.dto.LoginResponse;
import im.swyp.teumteumeat.global.security.dto.GoogleLoginRequest;
import im.swyp.teumteumeat.global.security.dto.request.SignUpRequest;
import im.swyp.teumteumeat.global.security.service.GoogleAuthService;
import im.swyp.teumteumeat.global.security.token.JwtProvider;
import im.swyp.teumteumeat.global.security.token.Token;
import im.swyp.teumteumeat.global.security.usecase.OAuth2UseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "인증 관련 API")
public class AuthController implements AuthApi {

    private final OAuth2UseCase oAuth2UseCase;
    private final GoogleAuthService googleAuthService;
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    @Override
    @PostMapping("/oauth/register")
    @PreAuthorize("isAnonymous()")
    public ResponseEntity<ApiResponse<LoginResponse>> oauthRegister(
            @RequestParam SocialProvider provider,
            @RequestBody @Valid SignUpRequest.Oidc request) {
        LoginResponse response = oAuth2UseCase.signUp(provider, request);
        return ResponseEntity.ok(ApiResponse.ofSuccess(CommonResponseCode.OK, response));
    }

    @Override
    @Deprecated
    @PostMapping("/google")
    public ApiResponse<LoginResponse> googleLogin(@RequestBody GoogleLoginRequest request) {
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
        Token token = jwtProvider.issueToken(user.getId(), user.getRole());

        return ApiResponse.ofSuccess(CommonResponseCode.OK, LoginResponse.builder()
                .accessToken(token.accessToken())
                .refreshToken(token.refreshToken())
                .build());
    }
}
