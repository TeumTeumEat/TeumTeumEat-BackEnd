package im.swyp.teumteumeat.domains.refreshtoken.domain.service;

import im.swyp.teumteumeat.domains.refreshtoken.persistence.repository.RefreshTokenRepository;
import im.swyp.teumteumeat.domains.refreshtoken.persistence.entity.RefreshToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    public void saveRefreshToken(Long userId, String refreshToken) {
        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .id(String.valueOf(userId))
                .refreshToken(refreshToken)
                .build();

        refreshTokenRepository.save(refreshTokenEntity);
    }
}