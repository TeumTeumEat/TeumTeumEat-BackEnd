package im.swyp.teumteumeat.global.security.service;

import im.swyp.teumteumeat.domains.user.persistence.entity.UserEntity;
import im.swyp.teumteumeat.domains.user.persistence.repository.UserRepository;
import im.swyp.teumteumeat.global.security.dto.CustomUserDetails;
import im.swyp.teumteumeat.global.security.dto.OAuth2Attributes;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 로그인 성공 이후 리소스 서버에서 사용자 정보(attributes)를 가져오는 클래스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Transactional
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("CustomOAuth2UserService.loadUser() executed");

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        log.info("Registration ID: {}", registrationId);

        Map<String, Object> attributes;
        String userNameAttributeName;

        if ("apple".equalsIgnoreCase(registrationId)) {
            // Apple은 UserInfo Endpoint를 호출하지 않고 id_token을 디코딩하여 정보를 가져옴
            String idToken = userRequest.getAdditionalParameters().get("id_token").toString();
            attributes = decodeJwtTokenPayload(idToken);
            attributes.put("id_token", idToken);
            attributes.put("sub", attributes.get("sub")); // Apple의 식별자는 sub
            userNameAttributeName = "sub";
        } else {
            // Kakao, Google 등은 기존 방식대로 UserInfo Endpoint 호출
            OAuth2User oAuth2User = super.loadUser(userRequest);
            attributes = oAuth2User.getAttributes();
            userNameAttributeName = userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint()
                    .getUserNameAttributeName();
        }

        // 유저 정보 생성
        OAuth2Attributes oAuth2Attributes = OAuth2Attributes.of(registrationId, attributes, userNameAttributeName);

        // 회원가입 및 로그인
        UserEntity user = getOrSaveUser(oAuth2Attributes);

        List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority(user.getRole().getKey())
        );

        // OAuth2User 반환
        return new CustomUserDetails(user.getId(), user.getRole(), authorities, oAuth2Attributes);
    }

    private UserEntity getOrSaveUser(OAuth2Attributes oAuth2Attributes) {
        return userRepository
                .findBySocialProviderAndSocialId(oAuth2Attributes.getProvider(), oAuth2Attributes.getProviderId())
                .orElseGet(() -> userRepository.save(
                        UserEntity.socialSignup(
                                oAuth2Attributes.getName(),
                                oAuth2Attributes.getEmail(),
                                oAuth2Attributes.getProvider(),
                                oAuth2Attributes.getProviderId())));
    }

    public Map<String, Object> decodeJwtTokenPayload(String jwtToken) {
        Map<String, Object> jwtClaims = new java.util.HashMap<>();
        try {
            String[] parts = jwtToken.split("\\.");
            java.util.Base64.Decoder decoder = java.util.Base64.getUrlDecoder();
            String payload = new String(decoder.decode(parts[1]));
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Map<String, Object> map = mapper.readValue(payload, Map.class);
            jwtClaims.putAll(map);
            return jwtClaims;
        } catch (Exception e) {
            log.error("Failed to parse JWT token", e);
        }
        return jwtClaims;
    }
}