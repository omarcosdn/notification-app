package com.github.omarcosdn.notification.core.services;

import java.util.UUID;

public interface MessageDispatcher {
  void dispatch(UUID tenantId, UUID idempotencyKey, String content);
}
