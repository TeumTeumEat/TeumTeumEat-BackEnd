package im.swyp.teumteumeat.domains.refreshtoken.persistence.entity;

import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

@Getter
@Builder
@RedisHash(value = "refreshToken")
public class RefreshToken {

    @Id
    private String id;

    @Indexed
    private String refreshToken;

    @TimeToLive
    private Long expiration;
}