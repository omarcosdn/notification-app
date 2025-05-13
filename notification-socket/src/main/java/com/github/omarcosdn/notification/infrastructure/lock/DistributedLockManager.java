package com.github.omarcosdn.notification.infrastructure.lock;

import java.time.Duration;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DistributedLockManager {

  private static final Duration LOCK_TTL = Duration.ofSeconds(10);
  private static final String LOCK_PREFIX = "lock:";

  private final StringRedisTemplate redisTemplate;

  public DistributedLockManager(StringRedisTemplate redisTemplate) {
    this.redisTemplate = Objects.requireNonNull(redisTemplate);
  }

  public boolean acquireLock(final UUID tenantId) {
    var lockKey = getLockKey(tenantId);
    var lockValue = UUID.randomUUID().toString();

    try {
      var acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, LOCK_TTL);

      if (Boolean.TRUE.equals(acquired)) {
        log.debug("Lock acquired for tenant {} with value {}", tenantId, lockValue);
        return true;
      }

      log.debug("Failed to acquire lock for tenant {}", tenantId);
      return false;
    } catch (Exception e) {
      log.error("Error acquiring lock for tenant {}", tenantId, e);
      return false;
    }
  }

  public boolean releaseLock(final UUID tenantId) {
    var lockKey = getLockKey(tenantId);

    try {
      var released = redisTemplate.delete(lockKey);

      if (Boolean.TRUE.equals(released)) {
        log.debug("Lock released for tenant {}", tenantId);
        return true;
      }

      log.debug("Failed to release lock for tenant {}", tenantId);
      return false;
    } catch (Exception e) {
      log.error("Error releasing lock for tenant {}", tenantId, e);
      return false;
    }
  }

  public boolean hasLock(final UUID tenantId) {
    var lockKey = getLockKey(tenantId);
    return Boolean.TRUE.equals(redisTemplate.hasKey(lockKey));
  }

  private String getLockKey(final UUID tenantId) {
    return LOCK_PREFIX + tenantId;
  }
}
