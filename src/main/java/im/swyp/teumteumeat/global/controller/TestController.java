package im.swyp.teumteumeat.global.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/api/v1/test/success")
    public String loginSuccess(@RequestParam("accessToken") String accessToken,
            @RequestParam("refreshToken") String refreshToken) {
        return "<h1>로그인 성공!</h1>" +
                "<p>Access Token: " + accessToken + "</p>" +
                "<p>Refresh Token: " + refreshToken + "</p>";
    }

    @org.springframework.beans.factory.annotation.Autowired
    private org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties oauth2ClientProperties;

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
