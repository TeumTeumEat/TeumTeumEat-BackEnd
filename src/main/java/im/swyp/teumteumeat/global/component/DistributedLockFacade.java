package im.swyp.teumteumeat.global.component;

import im.swyp.teumteumeat.global.common.CommonResponseCode;
import im.swyp.teumteumeat.global.exception.BaseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
@Component
@RequiredArgsConstructor
public class DistributedLockFacade {

    private final RedissonClient redissonClient;

    /**
     * 분산 락 작업을 수행
     *
     * @param lockKey   락 키
     * @param waitTime  락 획득 대기 시간
     * @param leaseTime 락 점유 시간
     * @param timeUnit  시간 단위
     * @param action    수행할 작업
     * @param <T>       반환 타입
     * @return 작업 결과
     */
    public <T> T executeWithLock(String lockKey, long waitTime, long leaseTime, TimeUnit timeUnit, Supplier<T> action) {
        RLock lock = redissonClient.getLock(lockKey);

        try {
            if (!lock.tryLock(waitTime, leaseTime, timeUnit)) {
                log.warn("Failed to acquire lock: {}", lockKey);
                throw new BaseException(CommonResponseCode.INTERNAL_SERVER_ERROR);
            }

            try {
                return action.get();
            } finally {
                if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Lock acquisition interrupted: {}", lockKey, e);
            throw new BaseException(CommonResponseCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 분산 락을 시도하고, 획득 성공 시 작업을 수행
     * 락 획득 실패(Timeout) 시 예외를 던지지 않고 빈 Optional을 반환
     */
    public <T> Optional<T> tryExecuteWithLock(String lockKey, long waitTime, long leaseTime,
            TimeUnit timeUnit, Supplier<T> action) {
        RLock lock = redissonClient.getLock(lockKey);

        try {
            if (!lock.tryLock(waitTime, leaseTime, timeUnit)) {
                log.warn("Failed to acquire lock: {}", lockKey);
                return Optional.empty();
            }

            try {
                return Optional.ofNullable(action.get());
            } finally {
                if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Lock acquisition interrupted: {}", lockKey, e);
            throw new BaseException(CommonResponseCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 특정 키에 대해 TTL을 설정하며 저장을 시도(SETNX).
     * @param key 상태 키
     * @param ttlSeconds 유지 시간(초)
     * @throws BaseException 이미 키가 존재할 경우(중복 요청) 예외 발생
     */
    public void checkAndSetCooldown(String key, long ttlSeconds) {
        boolean isFirstRequest = redissonClient.getBucket(key)
                .trySet("PROCESSING", ttlSeconds, TimeUnit.SECONDS);

        if (!isFirstRequest) {
            log.warn("Duplicate request blocked by cooldown key: {}", key);
            throw new BaseException(CommonResponseCode.TOO_MANY_REQUESTS);
        }
    }

    /**
     * 쿨타임(상태) 키를 삭제.
     * @param key 상태 키
     */
    public void deleteCooldownKey(String key) {
        try {
            redissonClient.getBucket(key).delete();
        } catch (Exception e) {
            log.error("Failed to delete cooldown key: {}", key, e);
        }
    }
}
