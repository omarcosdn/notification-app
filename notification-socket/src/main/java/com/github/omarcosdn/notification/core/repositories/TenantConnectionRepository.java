package com.github.omarcosdn.notification.core.repositories;

import java.util.UUID;

public interface TenantConnectionRepository {
  Boolean hasTenant(UUID tenantId);

  void register(UUID tenantId);

  void unregister(UUID tenantId);

  void refresh(UUID tenantId);
}
