package im.swyp.teumteumeat.domains.refreshtoken.domain.service;

import im.swyp.teumteumeat.global.security.constant.SocialProvider;
import im.swyp.teumteumeat.domains.refreshtoken.persistence.repository.RefreshTokenRepository;
import im.swyp.teumteumeat.domains.refreshtoken.persistence.entity.RefreshToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static im.swyp.teumteumeat.global.common.Constants.DELIMITER;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    public void saveRefreshToken(String refreshToken, SocialProvider socialProvider, String socialId) {
        String key = createKey(socialProvider, socialId);

        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .id(key)
                .refreshToken(refreshToken)
                .build();

        refreshTokenRepository.save(refreshTokenEntity);
    }

    private String createKey(SocialProvider socialProvider, String socialId) {
        return socialProvider + DELIMITER + socialId;
    }
}