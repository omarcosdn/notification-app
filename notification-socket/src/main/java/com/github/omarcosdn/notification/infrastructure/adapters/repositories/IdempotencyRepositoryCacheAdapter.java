package com.github.omarcosdn.notification.infrastructure.adapters.repositories;

import com.github.omarcosdn.notification.core.repositories.IdempotencyRepository;
import java.time.Duration;
import java.util.Objects;
import java.util.UUID;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class IdempotencyRepositoryCacheAdapter implements IdempotencyRepository {

  private static final Duration TTL = Duration.ofDays(1);

  private final StringRedisTemplate redisTemplate;

  public IdempotencyRepositoryCacheAdapter(final StringRedisTemplate redisTemplate) {
    this.redisTemplate = Objects.requireNonNull(redisTemplate);
  }

  @Override
  public Boolean registerIfAbsent(final UUID idempotencyKey) {
    var key = getKey(idempotencyKey);
    var set = redisTemplate.opsForValue().setIfAbsent(key, "processed", TTL);
    return Boolean.TRUE.equals(set);
  }

  private String getKey(final UUID idempotencyKey) {
    return "idempotency:" + idempotencyKey;
  }
}
