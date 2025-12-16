package im.swyp.teumteumeat.global.controller;

import im.swyp.teumteumeat.domains.user.domain.service.UserService;
import im.swyp.teumteumeat.domains.user.persistence.entity.UserEntity;
import im.swyp.teumteumeat.global.security.token.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final JwtProvider jwtProvider;
    private final UserService userService;
    private final im.swyp.teumteumeat.domains.user.persistence.repository.UserRepository userRepository;

    @GetMapping("/api/v1/test/token")
    public String generateTestToken(@RequestParam(required = false) Long userId,
            @RequestParam(required = false) String role) {
        UserEntity user;

        if (userId != null) {
            user = userService.getUserById(userId);
        } else {
            user = userRepository
                    .findBySocialProviderAndSocialId(im.swyp.teumteumeat.global.security.constant.SocialProvider.KAKAO,
                            "TEST_USER")
                    .orElseGet(() -> userRepository.save(UserEntity.socialSignup("Test User", "test@test.com",
                            im.swyp.teumteumeat.global.security.constant.SocialProvider.KAKAO, "TEST_USER")));
        }

        if (role != null) {
            user.updateRole(im.swyp.teumteumeat.domains.user.domain.constant.Role.valueOf(role));
            userRepository.save(user);
        }

        im.swyp.teumteumeat.global.security.token.TokenClaim tokenClaim = im.swyp.teumteumeat.global.security.token.TokenClaim
                .builder()
                .socialProvider(user.getSocialProvider())
                .socialId(user.getSocialId())
                .role(user.getRole())
                .build();

        String accessToken = jwtProvider.generateAccessToken(tokenClaim);
        String refreshToken = jwtProvider.generateRefreshToken(tokenClaim);

        return "<h1>Test Token Generated</h1>" +
                "<p>User ID: " + user.getId() + "</p>" +
                "<p>Role: " + user.getRole() + "</p>" +
                "<p>Access Token:<br><textarea rows='5' cols='100'>" + accessToken + "</textarea></p>" +
                "<p>Refresh Token:<br><textarea rows='5' cols='100'>" + refreshToken + "</textarea></p>";
    }

    @GetMapping("/api/v1/test/success")
    public String loginSuccess(@RequestParam("accessToken") String accessToken,
            @RequestParam("refreshToken") String refreshToken) {
        return "<h1>로그인 성공!</h1>" +
                "<p>Access Token: " + accessToken + "</p>" +
                "<p>Refresh Token: " + refreshToken + "</p>";
    }

    @Autowired
    private OAuth2ClientProperties oauth2ClientProperties; // RequiredArgsConstructor will inject final fields, leave
                                                           // this one autowired or move to constructor

    @GetMapping("/api/v1/test/failure")
    public String loginFailure() {
        return "<h1 style='color:red;'>로그인 실패!</h1>" +
                "<p>서버 로그를 확인해서 정확한 원인을 찾아보세요.</p>";
    }

    @GetMapping("/api/v1/test/config")
    public String checkConfig() {
        var kakao = oauth2ClientProperties.getRegistration().get("kakao");
        if (kakao == null)
            return "Kakao settings not found!";

        return "<h1>Kakao Configuration</h1>" +
                "<p>Client ID: " + kakao.getClientId() + "</p>" +
                "<p>Redirect URI: " + kakao.getRedirectUri() + "</p>" +
                "<p>Auth Method: " + kakao.getClientAuthenticationMethod() + "</p>" +
                "<p>Client Secret: " + (kakao.getClientSecret() == null ? "NULL"
                        : "EXISTS (Length: " + kakao.getClientSecret().length() + ")")
                + "</p>";
    }
}
