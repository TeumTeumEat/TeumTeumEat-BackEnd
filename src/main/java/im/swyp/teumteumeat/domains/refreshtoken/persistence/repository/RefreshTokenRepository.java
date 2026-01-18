package im.swyp.teumteumeat.domains.refreshtoken.persistence.repository;

import im.swyp.teumteumeat.domains.refreshtoken.persistence.entity.RefreshToken;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
    Optional<RefreshToken> findByRefreshToken(String refreshToken);
    boolean existsById(String refreshToken);
    void deleteById(String refreshToken);
    List<RefreshToken> findAllByUserId(Long userId);
}