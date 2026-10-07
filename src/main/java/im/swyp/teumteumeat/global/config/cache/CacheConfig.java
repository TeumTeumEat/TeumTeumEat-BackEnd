package im.swyp.teumteumeat.global.config.cache;

import java.time.Duration;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import im.swyp.teumteumeat.domains.league.application.component.LeagueRankingProvider;
import im.swyp.teumteumeat.domains.league.domain.vo.LeagueRanking;

@EnableCaching
@Configuration
public class CacheConfig {
	private static final long OIDC_CACHE_TTL_DAY = 1;
	// 스낵 적립 시 캐시를 삭제하므로 TTL은 삭제가 누락됐을 때의 안전장치
	private static final long LEAGUE_RANKING_CACHE_TTL_MINUTES = 5;

	@Bean
	public CacheManager redisCacheManager(RedisConnectionFactory cf) {
		RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
			.serializeKeysWith(
				RedisSerializationContext.SerializationPair.fromSerializer(
					new StringRedisSerializer()
				))
			.serializeValuesWith(
				RedisSerializationContext.SerializationPair.fromSerializer(
					new GenericJackson2JsonRedisSerializer()
				))
			.entryTtl(Duration.ofDays(OIDC_CACHE_TTL_DAY));

		// GenericJackson2JsonRedisSerializer는 record 등 final 클래스의 타입 정보를 남기지 않아
		// 역직렬화 시 LinkedHashMap이 되므로, 리그 랭킹은 타입을 지정한 직렬화기를 사용
		RedisCacheConfiguration leagueRankingConfig = config
			.serializeValuesWith(
				RedisSerializationContext.SerializationPair.fromSerializer(
					jsonSerializer(LeagueRanking.class)
				))
			.entryTtl(Duration.ofMinutes(LEAGUE_RANKING_CACHE_TTL_MINUTES));

		return RedisCacheManager
			.RedisCacheManagerBuilder
			.fromConnectionFactory(cf)
			.cacheDefaults(config)
			.withCacheConfiguration(LeagueRankingProvider.CACHE_NAME, leagueRankingConfig)
			.build();
	}

	public static <T> Jackson2JsonRedisSerializer<T> jsonSerializer(Class<T> type) {
		ObjectMapper objectMapper = new ObjectMapper()
			.registerModule(new JavaTimeModule())
			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		return new Jackson2JsonRedisSerializer<>(objectMapper, type);
	}
}
