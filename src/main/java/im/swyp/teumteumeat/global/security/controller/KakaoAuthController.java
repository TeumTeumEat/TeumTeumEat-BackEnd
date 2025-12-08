package im.swyp.teumteumeat.global.security.controller;

import im.swyp.teumteumeat.global.common.ApiResponse;
import im.swyp.teumteumeat.global.common.CommonResponseCode;
import im.swyp.teumteumeat.global.security.KakaoUtil;
import im.swyp.teumteumeat.global.security.dto.KakaoDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class KakaoAuthController {

    private final KakaoUtil kakaoUtil;

    @GetMapping("/auth/login/kakao")
    public ApiResponse<KakaoDTO.KakaoProfile> kakaoCallback(@RequestParam("code") String code) {
        log.info("카카오 로그인 콜백 - 인가 코드: {}", code);

        // 1. Access Token 발급
        KakaoDTO.OAuthToken token = kakaoUtil.requestToken(code);
        log.info("카카오 토큰 발급 완료 - Access Token: {}...",
                token.getAccess_token().substring(0, Math.min(10, token.getAccess_token().length())));

        // 2. 유저 프로필 요청
        KakaoDTO.KakaoProfile profile = kakaoUtil.requestProfile(token);
        log.info("카카오 프로필 조회 완료 - ID: {}, 닉네임: {}",
                profile.getId(),
                profile.getKakao_account().getProfile().getNickname());

        // 3. 프로필 정보 반환 (테스트용)
        // TODO: 실제 프로덕션에서는 여기서 DB에 유저 저장/조회, JWT 발급 등 처리
        return ApiResponse.ofSuccess(CommonResponseCode.OK, profile);
    }
}
