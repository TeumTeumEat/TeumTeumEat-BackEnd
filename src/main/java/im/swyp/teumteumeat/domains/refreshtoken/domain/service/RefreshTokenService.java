package im.swyp.teumteumeat.domains.refreshtoken.domain.service;

import im.swyp.teumteumeat.domains.refreshtoken.persistence.repository.RefreshTokenRepository;
import im.swyp.teumteumeat.domains.refreshtoken.persistence.entity.RefreshToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    public void saveRefreshToken(Long userId, String refreshToken, long ttl) {
        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .userId(userId)
                .refreshToken(refreshToken)
                .expiration(ttl)
                .build();

        refreshTokenRepository.save(refreshTokenEntity);
    }

    public void deleteRefreshToken(Long userId, String refreshToken) {
        refreshTokenRepository.findById(refreshToken)
                .ifPresent(storedToken -> {
                    if (storedToken.getUserId().equals(userId)) {
                        refreshTokenRepository.deleteById(refreshToken);
                    }
                });
    }

    public void deleteAllRefreshToken(Long userId) {
        List<RefreshToken> tokens = refreshTokenRepository.findAllByUserId(userId);

        refreshTokenRepository.deleteAll(tokens);
    }

    public boolean existRefreshToken(String refreshToken) {
        return refreshTokenRepository.existsById(refreshToken);
    }
}