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
}
