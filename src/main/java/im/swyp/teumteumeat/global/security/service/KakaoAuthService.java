package im.swyp.teumteumeat.global.security.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import im.swyp.teumteumeat.global.common.CommonResponseCode;
import im.swyp.teumteumeat.global.exception.BaseException;
import im.swyp.teumteumeat.global.security.constant.AuthResponseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoAuthService {

    private final ObjectMapper objectMapper;
    private final RestClient restClient;
    private static final String KAKAO_USER_ME_URL = "https://kapi.kakao.com/v2/user/me";

    public Map<String, Object> verifyIdToken(String idToken) {
        try {
            return restClient.get()
                    .uri(KAKAO_USER_ME_URL)
                    .header("Authorization", "Bearer " + idToken)
                    .header("Content-type", "application/x-www-form-urlencoded;charset=utf-8")
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        Map<String, Object> errorAttributes = objectMapper.readValue(
                                response.getBody(),
                                new TypeReference<>() {
                                }
                        );

                        int errorCode = (int) errorAttributes.getOrDefault("code", 0);
                        log.error("Kakao verify token failed. code: {}, body: {}",
                                errorCode, errorAttributes);

                        switch (errorCode) {
                            case -401: // 토큰 만료, 유효하지 않음
                            case -2: // 잘못된 형식
                                throw new BaseException(AuthResponseCode.INVALID_JWT_TOKEN);
                            case -1: // 카카오 내부 장애
                                throw new BaseException(CommonResponseCode.INTERNAL_SERVER_ERROR);
                            default:
                                throw new BaseException(CommonResponseCode.INTERNAL_SERVER_ERROR);
                        }
                    })
                    .body(new ParameterizedTypeReference<>() {
                    });
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to verify Kakao ID token", e);
            throw new BaseException(CommonResponseCode.INTERNAL_SERVER_ERROR);
        }
    }
}
