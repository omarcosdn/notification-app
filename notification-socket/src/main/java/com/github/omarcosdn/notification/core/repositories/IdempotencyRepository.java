package com.github.omarcosdn.notification.core.repositories;

import java.util.UUID;

public interface IdempotencyRepository {
  Boolean registerIfAbsent(UUID idempotencyKey);
}
