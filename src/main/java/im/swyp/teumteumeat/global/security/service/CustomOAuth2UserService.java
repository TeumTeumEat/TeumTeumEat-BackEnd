package im.swyp.teumteumeat.global.security.service;

import im.swyp.teumteumeat.domains.user.persistence.entity.UserEntity;
import im.swyp.teumteumeat.domains.user.persistence.repository.UserRepository;
import im.swyp.teumteumeat.global.security.dto.CustomUserDetails;
import im.swyp.teumteumeat.global.security.dto.OAuth2Attributes;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 로그인 성공 이후 리소스 서버에서 사용자 정보(attributes)를 가져오는 클래스
 */
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Transactional
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        // 식별자에 접근할 때 사용되는 값 e.g. "sub"
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 유저 정보 생성
        OAuth2Attributes oAuth2Attributes = OAuth2Attributes.of(registrationId, attributes, userNameAttributeName);

        // 회원가입 및 로그인
        UserEntity user = getOrSaveUser(oAuth2Attributes);

        // OAuth2User 반환
        return new CustomUserDetails(user, oAuth2Attributes);
    }

    private UserEntity getOrSaveUser(OAuth2Attributes oAuth2Attributes) {
        return userRepository.findBySocialProviderAndSocialId(oAuth2Attributes.getProvider(), oAuth2Attributes.getProviderId())
                .orElseGet(() -> userRepository.save(
                                UserEntity.socialSignup(
                                        oAuth2Attributes.getName(),
                                        oAuth2Attributes.getEmail(),
                                        oAuth2Attributes.getProvider(),
                                        oAuth2Attributes.getProviderId()
                                )
                        )
                );
    }

}