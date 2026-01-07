package im.swyp.teumteumeat.domains.user.application.usecase;

import im.swyp.teumteumeat.domains.categoryDocument.domain.service.CategoryDocumentService;
import im.swyp.teumteumeat.domains.goal.domain.service.GoalService;
import im.swyp.teumteumeat.domains.user.domain.service.UserService;
import im.swyp.teumteumeat.domains.user.persistence.entity.UserEntity;
import im.swyp.teumteumeat.domains.userQuiz.domain.service.UserQuizService;
import im.swyp.teumteumeat.global.security.AppleUtil;
import im.swyp.teumteumeat.global.security.constant.SocialProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import im.swyp.teumteumeat.global.annotation.UseCase;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class UserWithdrawalUseCase {

    private final UserService userService;
    private final AppleUtil appleUtil;
    private final RestTemplate restTemplate;
    private final UserQuizService userQuizService;

    @Value("${spring.security.oauth2.client.registration.kakao.admin-key:}")
    private String kakaoAdminKey;

    @Value("${spring.security.oauth2.client.registration.apple.client-id:}")
    private String appleClientId;

    @Transactional
    public void withdraw(Long userId) {
        UserEntity user = userService.getUserById(userId);

        SocialProvider provider = user.getSocialProvider();
        String socialId = user.getSocialId();

        log.info("Processing withdrawal for user: {} (Provider: {})", userId, provider);

        switch (provider) {
            case KAKAO:
                unlinkKakao(socialId);
                break;
            case GOOGLE:
                revokeGoogle(user.getSocialRefreshToken());
                break;
            case APPLE:
                revokeApple(user.getSocialRefreshToken());
                break;
            default:
                log.warn("Unknown provider for withdrawal: {}", provider);
        }

        // 명시적으로 삭제
        userQuizService.deleteAllByUserId(userId);

        userService.deleteUser(user);
        log.info("User {} deleted.", userId);
    }

    private void unlinkKakao(String socialId) {
        if (kakaoAdminKey == null || kakaoAdminKey.isBlank()) {
            log.error("Kakao Admin Key is not configured. Skipping unlink.");
            return;
        }

        String url = "https://kapi.kakao.com/v1/user/unlink";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "KakaoAK " + kakaoAdminKey);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("target_id_type", "user_id");
        body.add("target_id", socialId);

        try {
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);
            // 응답 본문 무시
            restTemplate.postForLocation(url, entity);
            log.info("Kakao unlink success for socialId: {}", socialId);
        } catch (Exception e) {
            log.error("Failed to unlink Kakao user: {}", socialId, e);
            // 연결 끊기에 실패하더라도 DB 삭제는 진행 (유저의 탈퇴 의도 존중)
        }
    }

    private void revokeGoogle(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            log.warn("Google refresh token not provided. Skipping Google revocation.");
            return;
        }

        try {
            String url = "https://oauth2.googleapis.com/revoke?token=" + refreshToken;
            restTemplate.postForLocation(url, null);
            log.info("Google revoke success.");
        } catch (Exception e) {
            log.error("Failed to revoke Google token", e);
        }
    }

    private void revokeApple(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            log.warn("Apple refresh token not provided. Skipping Apple revocation.");
            return;
        }

        try {
            String clientSecret = appleUtil.createClientSecret();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            // 2. 토큰 철회 (Revoke)
            String revokeUrl = "https://appleid.apple.com/auth/revoke";
            MultiValueMap<String, String> revokeBody = new LinkedMultiValueMap<>();
            revokeBody.add("client_id", appleClientId);
            revokeBody.add("client_secret", clientSecret);
            revokeBody.add("token", refreshToken);
            revokeBody.add("token_type_hint", "refresh_token");

            HttpEntity<MultiValueMap<String, String>> revokeRequest = new HttpEntity<>(revokeBody, headers);
            restTemplate.postForLocation(revokeUrl, revokeRequest);
            log.info("Apple revoke success.");

        } catch (Exception e) {
            log.error("Failed to revoke Apple user", e);
            // DB 삭제 진행
        }
    }
}
