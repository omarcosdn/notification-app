package com.github.omarcosdn.notification.infrastructure.adapters.repositories;

import com.github.omarcosdn.notification.core.repositories.TenantConnectionRepository;
import java.time.Duration;
import java.util.Objects;
import java.util.UUID;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class TenantConnectionRepositoryCacheAdapter implements TenantConnectionRepository {

  private static final Duration TENANT_TTL = Duration.ofSeconds(120);

  private final StringRedisTemplate redisTemplate;

  public TenantConnectionRepositoryCacheAdapter(final StringRedisTemplate redisTemplate) {
    this.redisTemplate = Objects.requireNonNull(redisTemplate);
  }

  @Override
  public Boolean hasTenant(final UUID tenantId) {
    var key = getTenantKey(tenantId);
    return redisTemplate.hasKey(key);
  }

  @Override
  public void register(final UUID tenantId) {
    var key = getTenantKey(tenantId);

    var wasSet = redisTemplate.opsForValue().setIfAbsent(key, "connected", TENANT_TTL);

    if (Boolean.FALSE.equals(wasSet)) {
      throw new IllegalStateException("Tenant already connected");
    }
  }

  @Override
  public void unregister(final UUID tenantId) {
    var key = getTenantKey(tenantId);
    redisTemplate.delete(key);
  }

  @Override
  public void refresh(final UUID tenantId) {
    var key = getTenantKey(tenantId);
    if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
      redisTemplate.expire(key, TENANT_TTL);
    }
  }

  private String getTenantKey(final UUID tenantId) {
    return "ws-session:" + tenantId;
  }
}
